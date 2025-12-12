# Compose Modular Android (Bootstrap)

This repo is a Kotlin-based Android project scaffold optimized for Jetpack Compose and future modularization.

## Modules

- **`:app`**
  - Android application entry point.
  - Compose UI, navigation, app-level DI wiring, WorkManager/Firebase integrations.
  - Owns the global theme **presentation** layer (Compose `MaterialTheme`, `CompositionLocal`s).

- **`:domain`**
  - Pure Kotlin module.
  - Contains business models and interfaces (e.g., repositories/use-cases).
  - No Android dependencies.

- **`:data`**
  - Android library module.
  - Implements `:domain` repositories.
  - Owns persistence/network implementations (DataStore, Room, Retrofit/OkHttp).

- **`:feature-chat`** (optional)
  - Example feature module (Android library today) intended to be convertible to a dynamic feature later.

## Theme system

The app uses an AMOLED-first dark palette (true black background) and exposes a configurable **accent color**:

- Persisted via **DataStore** (`:data`).
- Observed as a `Flow` and surfaced to Compose via `CompositionLocal` + `MaterialTheme` (`:app`).

