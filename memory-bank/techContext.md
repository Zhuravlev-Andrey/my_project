# Tech Context

## Стек (проверено)

- Android, Kotlin 2.2.10, Compose BOM 2026.02.01, Material 3.
- AGP 9.5.1, Gradle 9.6.0, Java 11.
- compileSdk/targetSdk 37, minSdk 24; namespace/applicationId `com.example.myapplication`.
- Один модуль `:app`; версии — `gradle/libs.versions.toml`.
- Configuration cache включён (`gradle.properties`); foojay resolver в `settings.gradle.kts`.

## Команды

- Сборка: `./gradlew build`
- Debug: `./gradlew assembleDebug`
- Установка: `./gradlew installDebug`
- Юнит-тесты: `./gradlew test`
- Инструментальные: `./gradlew connectedAndroidTest`
- Линт: `./gradlew lint`

## Ограничения и грабли

- Приложение полностью офлайн (ADR-004): не добавлять сеть, серверные API, аккаунты,
  синхронизацию и аналитику без пересмотра решения.
- `app/src/main/start.kts` — черновик Room-модели в пакете `com.example.workout.data`.
  Лежит вне `java/`, не компилируется; зависимостей Room в `app/build.gradle.kts` нет.
  Перед использованием перенести в `java/...` и добавить Room/KSP.
- Release включает R8 (`optimization.enable = true`) с `packageScope` для androidx/kotlin/kotlinx.
- Keep-правила — в `app/src/main/keepRules/*.keep`, не в `proguard-rules.pro`.
- Нет CI, отдельного конфига линтера и продуктовых тестов; тесты в `app/src/test` и
  `app/src/androidTest` — шаблонные.
- `local.properties`, `.gradle/`, `build/` не коммитить; `*.jks`/`*.keystore` игнорируются.
