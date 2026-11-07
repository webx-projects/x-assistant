# My AI Assistant

A voice-first personal assistant for tasks, reminders, notes, income and expenses. The app is designed around local-first storage, optional cloud backup, and an AI gateway integration.

## Product decisions
- Android native app: Kotlin, Jetpack Compose, Material 3.
- Languages: Hindi, English and Hinglish.
- Voice: push-to-talk first; hands-free activation only through Android-supported APIs and user-visible permissions.
- Storage: Room as the local source of truth; optional backup is separate from sync.
- AI: OmniRoute gateway at `https://ai.vmstudio.digital`. Credentials must never be committed to this repository or embedded as private secrets in the APK.

## Architecture
Clean Architecture with MVVM and feature-oriented modules/packages:
- `core`: common UI, navigation, database, networking, security and utilities.
- `data`: Room entities/DAOs, repositories and remote gateway implementation.
- `domain`: use cases and business models.
- `feature`: dashboard, assistant, tasks, finance, reminders, notes and settings.

## Safety and platform constraints
- The LLM never receives direct SQL or arbitrary code execution access.
- Financial calculations are deterministic application logic.
- Destructive actions require confirmation.
- Background microphone and wake-word behavior must comply with Android platform restrictions; no hidden recording or AccessibilityService workaround.

## Development status
Initial repository bootstrap. Android project files and implementation will be added incrementally. Build validation requires Android SDK/Gradle in a developer environment.
