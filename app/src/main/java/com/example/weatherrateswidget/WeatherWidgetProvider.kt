package com.example.weatherrateswidget

import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
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

            manager.updateAppWidget(id, views)
        }
    }
}
