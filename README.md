# My AI Assistant

A voice-first personal assistant for tasks, reminders, notes, income and expenses. The app is designed around local-first storage, optional cloud backup, and an AI gateway integration.

## Product decisions
- Android native app: Kotlin, Jetpack Compose, Material 3.
- Languages: Hindi, English and Hinglish.
- Voice: push-to-talk first; hands-free activation only through Android-supported APIs and user-visible permissions.
- Storage: Room as the local source of truth; optional backup is separate from sync.
- AI: OmniRoute gateway at `https://ai.vmstudio.digital`. Credentials must never be committed to this repository or embedded as private secrets in the APK.

## Architecture
Clean Architecture with MVVM and feature-oriented packages:
- `core`: shared UI, navigation, database, networking, security and utilities.
- `domain`: business models, repository contracts and use cases.
- `data`: Room entities/DAOs, repositories and remote gateway implementation.
- `presentation`: dashboard and feature screens.

## Current local data foundation
Room database v1 stores tasks, income/expense transactions, reminders and notes. Finance amounts are stored as integer minor units (paise for INR), not floating-point values. The repository exposes typed operations and Flow-based observations. AI must call validated use cases and must never access SQL directly.

## Build
Open the repository in a current Android Studio release with Android SDK API 37 installed. The GitHub Actions workflow runs `gradle assembleDebug testDebugUnitTest` using Gradle 9.6.0 and JDK 21.

## Safety and platform constraints
- The LLM never receives direct SQL or arbitrary code execution access.
- Financial calculations are deterministic application logic.
- Destructive actions require explicit confirmation.
- Background microphone and wake-word behavior must comply with Android platform restrictions; no hidden recording or AccessibilityService workaround.

## Development status
See [DEVELOPMENT_STATUS.md](DEVELOPMENT_STATUS.md) for implemented work, outstanding features and verification status.
