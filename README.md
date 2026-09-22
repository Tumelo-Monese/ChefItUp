# Chef it Up 

A Kotlin + Jetpack Compose recipe app built for South African kitchens — search recipes, cook with what's already in your fridge, plan meals, build shopping lists, and level up as a chef along the way.

The app talks to a local ChefItUp.Api middle layer so Spoonacular API keys never live on-device.

## Features

- Auth & onboarding — email/password and Google sign-in via Firebase Auth
- Recipe search & "cook with ingredients" — find recipes from what you already have
- Meal planner — plan meals across the week
- Shopping list — auto-generated and mergeable from planned meals
- Cooking mode— step-by-step flow with built-in timers and text-to-speech
- Gamification— XP, chef levels, and badges as you cook
- Offline support — Room cache with WorkManager background sync
- Notifications — meal reminders and recipe recommendations (toggle in Settings)
- Multi-language — English, Afrikaans, isiZulu, and Sesotho

## Tech stack

| Layer | Tech |
|---|---|
| UI | Jetpack Compose, Material 3, Navigation Compose |
| Architecture | MVVM, Hilt (DI), Coroutines/Flow |
| Local storage | Room, DataStore Preferences |
| Networking | Retrofit, OkHttp, Gson |
| Background work | WorkManager |
| Backend services | Firebase Auth, Firestore, Cloud Messaging (FCM) |
| Images | Coil |
| Language | Kotlin, JVM target 17 |

## Requirements

- Android Studio (Ladybug or newer) with AGP 8.7
- JDK 17
- An emulator or device running Android 7.0 (API 24) or later
- Optional: [.NET 8 SDK](https://dotnet.microsoft.com/download/dotnet/8.0) to run the companion `ChefItUp.Api` project
- Optional: a Firebase project — `app/google-services.json` is required for Auth, Firestore, and FCM

## Getting started

### 1. Clone and open

Open the project root in Android Studio and let Gradle sync.

### 2. Run the API (recommended)

The app is built to talk to the `ChefItUp.Api` middle layer, which sits alongside this repo:

```powershell
cd ..\ChefItUp.Api
dotnet run --urls http://0.0.0.0:5080
```

- From an emulator, the app reaches the host machine automatically at `http://10.0.2.2:5080/` — this is already set as `BuildConfig.API_BASE_URL`.
- From a physical device, point the app at your PC's LAN IP instead, e.g. `http://192.168.1.20:5080/`, by updating `API_BASE_URL` in `app/build.gradle.kts`.
- No Spoonacular key on the server? No problem — the API (and therefore the app) fall back to rich mock recipe data.

### 3. Configure the shared API key (optional)

If you've set `ChefItUp:ApiKey` on the server, mirror the same value in the app:

```kotlin
buildConfigField("String", "API_KEY", "\"your-shared-secret\"")
```

When `API_KEY` is non-empty, the app automatically sends it as the `X-ChefItUp-Key` header on every request.

### 4. Build and run

Use Android Studio's Run button, or from the command line:

```powershell
.\gradlew.bat installDebug
```

## Network & security notes

- **Debug builds** allow cleartext (non-HTTPS) traffic only to `10.0.2.2`, `localhost`, and `127.0.0.1`, as configured in `res/xml/network_security_config.xml`.
- **Release builds** have minification and resource shrinking enabled (see `proguard-rules.pro`). Point `API_BASE_URL` at an HTTPS host before shipping a release build.

## Project layout

```
app/src/main/java/com/chefitup/app/
├── presentation/   # Compose screens & ViewModels (auth, home, search, cook,
│                   #   cooking, mealplan, shopping, saved, recipe, profile,
│                   #   settings, onboarding, splash, navigation, theme)
├── domain/         # Models, repository contracts, gamification & validation rules
├── data/           # Room DB, Retrofit/OkHttp networking, Firebase, repositories, sync
├── notifications/  # FCM handling + reminder WorkManager workers
├── di/             # Hilt modules
└── util/           # Shared helpers (e.g. ingredient scaling, shopping list merging)
```

## Testing

Run the unit test suite:

```powershell
.\gradlew.bat test
```

Tests cover gamification rules (XP/level progression), auth validation, ingredient scaling, and shopping list merging.

## Localization

Translated string resources live under `res/values-<lang>/` (`af`, `zu`, `st`). The helper script at `scripts/sync_locale_strings.py` can be used to keep locale files in sync with the base `strings.xml`.
