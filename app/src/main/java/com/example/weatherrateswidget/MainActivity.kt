package com.example.weatherrateswidget

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.work.*
import java.util.concurrent.TimeUnit

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        scheduleUpdates()
        setContent {
            MaterialTheme {
                MainScreen()
            }
        }
    }

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
            ExistingPeriodicWorkPolicy.UPDATE,
            request
        )
    }
}

@Composable
private fun MainScreen() {
    var city by remember { mutableStateOf("Харьков") }
    var status by remember { mutableStateOf("Данные будут загружены автоматически") }

    Surface(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(20.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text("Погода и курсы", style = MaterialTheme.typography.headlineMedium)
            Text(
                "Компактный виджет 4×2 для рабочего стола",
                style = MaterialTheme.typography.bodyMedium
            )

            OutlinedTextField(
                value = city,
                onValueChange = { city = it },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Город") },
                singleLine = true
            )

            Button(
                onClick = {
                    AppPrefs.setCity(city)
                    status = "Город сохранён. Обновление запущено."
                    UpdateWorker.runNow()
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Default.Refresh, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text("Сохранить и обновить")
            }

            Card(modifier = Modifier.fillMaxWidth()) {
                Column(Modifier.padding(16.dp)) {
                    Text("Виджет", style = MaterialTheme.typography.titleMedium)
                    Spacer(Modifier.height(8.dp))
                    Text("Время обновляется непосредственно на экране телефона.")
                    Text("Погода и курсы обновляются в фоне при наличии интернета.")
                }
            }

            Card(modifier = Modifier.fillMaxWidth()) {
                Row(
                    Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.WbSunny, contentDescription = null)
                    Spacer(Modifier.width(12.dp))
                    Column {
                        Text("Погода", style = MaterialTheme.typography.titleMedium)
                        Text(AppPrefs.getTemp().let { if (it.isBlank()) "—" else it })
                    }
                }
            }

            Card(modifier = Modifier.fillMaxWidth()) {
                Row(
                    Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.Cloud, contentDescription = null)
                    Spacer(Modifier.width(12.dp))
                    Column {
                        Text("Курсы НБУ", style = MaterialTheme.typography.titleMedium)
                        Text("USD: ${AppPrefs.getUsd().ifBlank { "—" }}")
                        Text("EUR: ${AppPrefs.getEur().ifBlank { "—" }}")
                    }
                }
            }

            Text(status, style = MaterialTheme.typography.bodySmall)
        }
    }
}
