package com.example.weatherrateswidget

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import org.json.JSONArray
import java.net.HttpURLConnection
import java.net.URLEncoder
import java.net.URL
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class UpdateWorker(appContext: Context, workerParams: WorkerParameters) :
    CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result = try {
        updateWeather()
        updateRates()
        AppPrefs.setLastUpdateMillis(System.currentTimeMillis())
        AppPrefs.setLastError(null)
        WeatherWidgetProvider.updateAll(applicationContext)
        Result.success()
    } catch (e: Exception) {
        AppPrefs.setLastError(e.message ?: e.javaClass.simpleName)
        // ponytail: cap retries at 3 instead of retrying forever on a persistent
        // failure (e.g. bad city name). Next periodic run (15 min) picks it back up.
        if (runAttemptCount < 3) Result.retry() else Result.failure()
    }

    private suspend fun updateWeather() = withContext(Dispatchers.IO) {
        val city = URLEncoder.encode(AppPrefs.getCity(), "UTF-8")
        val geoUrl = URL(
            "https://geocoding-api.open-meteo.com/v1/search?name=$city&count=1&language=ru&format=json"
        )
        val geo = read(geoUrl)
        val results = geo.optJSONArray("results") ?: return@withContext
        if (results.length() == 0) return@withContext
        val place = results.getJSONObject(0)
        val lat = place.getDouble("latitude")
        val lon = place.getDouble("longitude")

        val weatherUrl = URL(
            "https://api.open-meteo.com/v1/forecast?latitude=$lat&longitude=$lon&current=temperature_2m,weather_code&timezone=auto"
        )
        val weather = read(weatherUrl).getJSONObject("current")
        val temp = weather.getDouble("temperature_2m")
        val code = weather.getInt("weather_code")

        AppPrefs.setTemp(String.format("%.0f°C", temp))
        AppPrefs.setWeatherCode(code)
    }

    // ponytail: PrivatBank returns all currencies in one response, so USD+EUR
    // are fetched together instead of two separate requests like before.
    // Showing "sale" (retail sell rate) only, to match the single-number
    // format the widget already displays — not the buy/sell spread.
    private suspend fun updateRates() = withContext(Dispatchers.IO) {
        val url = URL("https://api.privatbank.ua/p24api/pubinfo?json&exchange&coursid=5")
        val arr = JSONArray(readText(url))
        for (i in 0 until arr.length()) {
            val entry = arr.getJSONObject(i)
            when (entry.getString("ccy")) {
                "USD" -> AppPrefs.setUsd(formatRate(entry.getString("sale")))
                "EUR" -> AppPrefs.setEur(formatRate(entry.getString("sale")))
            }
        }
    }

    // ponytail: PrivatBank returns 5 decimal places ("45.05000"); round to the
    // 2 the widget actually displays instead of showing the raw string.
    private fun formatRate(raw: String) = String.format("%.2f", raw.toDouble())

    private fun read(url: URL): org.json.JSONObject =
        org.json.JSONObject(readText(url))

    private fun readText(url: URL): String {
        val conn = url.openConnection() as HttpURLConnection
        conn.connectTimeout = 10000
        conn.readTimeout = 10000
        conn.requestMethod = "GET"
        return conn.inputStream.bufferedReader().use { it.readText() }
            .also { conn.disconnect() }
    }

    companion object {
        fun runNow() {
            val request = OneTimeWorkRequestBuilder<UpdateWorker>().build()
            WorkManager.getInstance().enqueue(request)
        }
    }
}
