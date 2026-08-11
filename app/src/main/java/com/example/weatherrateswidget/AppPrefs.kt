package com.example.weatherrateswidget

import android.content.Context

object AppPrefs {
    private lateinit var context: Context

    fun init(context: Context) {
        this.context = context.applicationContext
    }

    private val p get() = context.getSharedPreferences("data", Context.MODE_PRIVATE)

    fun setCity(city: String) { p.edit().putString("city", city.trim()).apply() }
    fun getCity(): String = p.getString("city", "Харьков") ?: "Харьков"

    fun setTemp(value: String) { p.edit().putString("temp", value).apply() }
    fun getTemp(): String = p.getString("temp", "") ?: ""

    fun setUsd(value: String) { p.edit().putString("usd", value).apply() }
    fun getUsd(): String = p.getString("usd", "") ?: ""

    fun setEur(value: String) { p.edit().putString("eur", value).apply() }
    fun getEur(): String = p.getString("eur", "") ?: ""

    fun setWeatherCode(value: Int) { p.edit().putInt("weather_code", value).apply() }
    fun getWeatherCode(): Int = p.getInt("weather_code", 0)

    fun setLastUpdateMillis(value: Long) { p.edit().putLong("last_update", value).apply() }
    fun getLastUpdateMillis(): Long = p.getLong("last_update", 0L)

    fun setLastError(message: String?) { p.edit().putString("last_error", message).apply() }
    fun getLastError(): String? = p.getString("last_error", null)
}
