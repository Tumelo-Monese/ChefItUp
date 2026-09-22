# Chef it Up (Android)

Kotlin + Jetpack Compose recipe app for South African kitchens. Talks to the local **ChefItUp.Api** middle layer (Spoonacular keys stay off-device).

## Requirements

- Android Studio Ladybug+ / AGP 8.7
- JDK 17
- Emulator API 24+ (or physical device)
- Optional: [.NET 8 SDK](https://dotnet.microsoft.com/download/dotnet/8.0) to run `../ChefItUp.Api`
- Optional: Firebase project (`app/google-services.json` is required for Auth / Firestore / FCM)

## Run the API (recommended)

```powershell
cd ..\ChefItUp.Api
dotnet run --urls http://0.0.0.0:5080
```

Emulator reaches the host at `http://10.0.2.2:5080/` (already set as `BuildConfig.API_BASE_URL`).

Physical device: set `API_BASE_URL` in `app/build.gradle.kts` to your PC LAN IP, e.g. `http://192.168.1.20:5080/`.

Without a Spoonacular key the API (and app) serve rich mock recipes.

## Configure API key (optional)

If you set `ChefItUp:ApiKey` on the server, mirror it in the app:

```kotlin
buildConfigField("String", "API_KEY", "\"your-shared-secret\"")
```

The app sends `X-ChefItUp-Key` automatically when `API_KEY` is non-empty.

## Network / HTTPS

- Debug: cleartext allowed only for `10.0.2.2`, `localhost`, `127.0.0.1` via `res/xml/network_security_config.xml`.
- Release: minify + shrink enabled; point `API_BASE_URL` at your HTTPS host.

## Features

- Auth (email/password + Google), onboarding, search, cook-with-ingredients
- Meal planner, shopping list, cooking mode (timer + TTS)
- XP / chef levels / badges
- Offline Room cache + WorkManager sync
- Meal reminder & recipe recommendation notifications (Settings toggles)
- Languages: English, Afrikaans, isiZulu, Sesotho

## Project layout

```
app/src/main/java/com/chefitup/app/
  presentation/   # Compose UI
  domain/         # models + repository contracts
  data/           # Room, Retrofit, Firebase, repos
  notifications/  # FCM + reminder workers
```

## Tests

```powershell
.\gradlew test
```
