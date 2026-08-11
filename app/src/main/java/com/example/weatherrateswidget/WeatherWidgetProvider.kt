package com.example.weatherrateswidget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class WeatherWidgetProvider : AppWidgetProvider() {

    override fun onUpdate(
        context: Context,
        manager: AppWidgetManager,
        ids: IntArray
    ) {
        AppPrefs.init(context)
        ids.forEach { update(context, manager, it) }
        UpdateWorker.runNow()
    }

    companion object {
        fun updateAll(context: Context) {
            AppPrefs.init(context)
            val manager = AppWidgetManager.getInstance(context)
            val component = ComponentName(context, WeatherWidgetProvider::class.java)
            manager.getAppWidgetIds(component).forEach {
                update(context, manager, it)
            }
        }

        private fun update(
            context: Context,
            manager: AppWidgetManager,
            id: Int
        ) {
            val views = RemoteViews(context.packageName, R.layout.weather_widget)
            val locale = Locale("ru", "RU")
            val date = SimpleDateFormat("EEE, d MMM", locale).format(Date())
                .replaceFirstChar { it.uppercase(locale) }

            views.setTextViewText(R.id.widget_date, date)
            views.setTextViewText(R.id.widget_city, AppPrefs.getCity())
            views.setTextViewText(
                R.id.widget_temperature,
                AppPrefs.getTemp().ifBlank { "—°C" }
            )
            views.setTextViewText(
                R.id.widget_usd,
                "USD ${AppPrefs.getUsd().ifBlank { "—" }}"
            )
            views.setTextViewText(
                R.id.widget_eur,
                "EUR ${AppPrefs.getEur().ifBlank { "—" }}"
            )
            views.setImageViewResource(
                R.id.widget_weather_icon,
                iconFor(AppPrefs.getWeatherCode())
            )

            val openApp = PendingIntent.getActivity(
                context,
                0,
                Intent(context, MainActivity::class.java),
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            views.setOnClickPendingIntent(R.id.widget_root, openApp)

            manager.updateAppWidget(id, views)
        }

        // Maps Open-Meteo's WMO weather codes to one of our four icons.
        // https://open-meteo.com/en/docs (WMO Weather interpretation codes)
        private fun iconFor(code: Int): Int = when (code) {
            0 -> R.drawable.ic_weather_widget // clear / sunny
            in 1..3, 45, 48 -> R.drawable.ic_weather_cloud
            in 51..67, in 80..82, in 95..99 -> R.drawable.ic_weather_rain
            in 71..77, in 85..86 -> R.drawable.ic_weather_snow
            else -> R.drawable.ic_weather_widget
        }
    }
}
