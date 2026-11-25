# my_project

Android-приложение на Kotlin и Jetpack Compose (стартовый шаблон Empty Activity).

## Стек

- Kotlin 2.2.10, Jetpack Compose (BOM 2026.02.01), Material 3
- AGP 9.5.1, Gradle 9.6.0
- minSdk 24, targetSdk 37

## Структура

- `app/src/main/java/com/example/myapplication/` — код приложения (`MainActivity.kt`, `ui/theme/`)
- `app/src/main/res/` — ресурсы: строки, темы, иконки
- `openspec/` — спецификации и запланированные изменения (OpenSpec)

## Команды

- Сборка: `./gradlew build`
- Debug-сборка: `./gradlew assembleDebug`
- Установка на эмулятор/устройство: `./gradlew installDebug`
- Юнит-тесты: `./gradlew test`
- Инструментальные тесты: `./gradlew connectedAndroidTest`
- Линт: `./gradlew lint`

## Правила

- Артефакты OpenSpec и тексты интерфейса — на русском.
- Изменения планировать через OpenSpec: `openspec propose`, затем `openspec apply`.
- Не коммитить `local.properties`, `.gradle/` и `build/`.
