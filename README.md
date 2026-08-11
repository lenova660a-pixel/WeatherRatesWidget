# WeatherRatesWidget

Android-приложение с виджетом 4×2.

## Что уже есть

- Material 3 интерфейс;
- выбор города;
- погода через Open-Meteo;
- USD/UAH и EUR/UAH через API НБУ;
- виджет 4×2;
- живые часы через `TextClock`;
- фоновые обновления через WorkManager;
- без платных API и без стороннего сервера.

## Открытие

Открыть папку в Android Studio и дождаться синхронизации Gradle.

## Сборка APK

Android Studio:
`Build → Generate App Bundle / APK → Generate APK`

GitHub Actions можно добавить следующим шагом.


## GitHub Actions

Файл автоматической сборки находится в:

`.github/workflows/build-apk.yml`

После загрузки проекта в GitHub:
1. Открой вкладку **Actions**.
2. Выбери **Build APK**.
3. Нажми **Run workflow**, если сборку нужно запустить вручную.
4. После завершения открой выполненный workflow.
5. В разделе **Artifacts** скачай `WeatherRatesWidget-debug`.

Также сборка запускается автоматически после push в ветку `main` или `master`.
