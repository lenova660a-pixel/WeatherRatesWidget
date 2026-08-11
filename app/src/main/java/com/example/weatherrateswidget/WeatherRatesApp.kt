package com.example.weatherrateswidget

import android.app.Application
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import java.util.concurrent.TimeUnit

class WeatherRatesApp : Application() {
    override fun onCreate() {
        super.onCreate()
        AppPrefs.init(this)
        scheduleUpdates()
    }

    // Scheduled here (not in MainActivity) so background updates keep running
    // even if the user only ever adds the widget and never opens the app.
    private fun scheduleUpdates() {
        val request = PeriodicWorkRequestBuilder<UpdateWorker>(15, TimeUnit.MINUTES)
            .setConstraints(
                Constraints.Builder()
                    .setRequiredNetworkType(NetworkType.CONNECTED)
                    .build()
            )
            .build()

        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
            "weather_rates_update",
            ExistingPeriodicWorkPolicy.KEEP,
            request
        )
    }
}
