package com.example.weatherrateswidget

import android.app.Application

class WeatherRatesApp : Application() {
    override fun onCreate() {
        super.onCreate()
        AppPrefs.init(this)
    }
}
