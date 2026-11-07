# Development Status

## Implemented in the current development branch
- Native Android app foundation using Kotlin, Jetpack Compose and Material 3.
- Dashboard and five-item navigation for Home, AI Assistant, Tasks, Money and More.
- Room-backed local tasks, transactions, reminders and notes with reactive repository streams.
- Task creation and completion/reopen; transaction and note creation; reminder creation.
- Finance values stored as integer minor units (paise for INR), with deterministic all-time totals.
- OmniRoute OpenAI-compatible chat client with typed app-owned tools for task, expense, income, reminder, note, open-task listing and finance summary.
- API key encrypted using Android Keystore; only ciphertext is stored in app-private preferences.
- Foreground push-to-talk speech recognition with runtime microphone permission.
- Text-to-speech replies, manual replay and a voice replies toggle.
- Android reminder notification channel, alarm scheduling, notification permission request, and rescheduling after reboot, clock or timezone changes.
- Reminder scheduling is connected to both manual and AI-created reminders. It uses Android's inexact idle-friendly alarm API; exact-alarm special access is not requested.
- Room database v2 includes local assistant chat history and a 1-to-2 migration. User and assistant messages are persisted, restored on opening the assistant, and can be cleared with confirmation.
- Finance transaction update support in the repository and DAO.
- AI tool execution is sequential, bounded to three rounds per turn, rejects unknown tools, and has no arbitrary SQL/code execution.
- Android CI now attempts a clean debug build and unit tests, then uploads the debug APK as a workflow artifact when successful.

## Remaining work
- Obtain a successful clean CI build and inspect the resulting APK artifact. The latest branch commits have not yet returned a confirmed successful workflow status.
- Add and commit a standard Gradle wrapper (the current CI installs Gradle 9.6.0 directly).
- Verify OmniRoute connectivity, authentication, model selection and tool calling using a real user-owned API key; the key must be entered locally in app settings, never shared in chat or committed.
- Add streaming responses, model discovery and robust retry/network recovery.
- Add complete edit/delete UI flows for tasks, transactions, reminders and notes, with confirmation for destructive actions.
- Add recurrence rules, snooze, notification action buttons and device-specific notification behavior testing.
- Add conversation context limits/summarization and richer chat controls.
- Add Room DAO/migration/repository tests, encrypted key-store tests, tool executor tests, Compose UI tests and device tests.
- Add date-range finance reports, category analytics, budgets, CSV/PDF export and payment method support.
- Add language/voice selection, voice interaction tests and investigate Android-supported assistant role/hotword integration on supported devices.
- Add optional encrypted backup/export-import and a documented restore flow.
- Add privacy disclosures, onboarding, app icon/splash, release signing, versioning and Play-ready release configuration.
- Review accessibility, large-font behavior, tablet layouts, offline/error states and battery/background restrictions.

## Verification
- Earlier CI attempts exposed and corrected the removed Android SDK tools package and a premature Lifecycle alpha dependency.
- A debug APK is not claimed as built or verified yet. Workflow status must be green and its artifact must be inspected before calling the app build-verified.
- The configured gateway URL is `https://ai.vmstudio.digital`; the deployed instance and available models have not been tested with a real API key.
- Reminder delivery is implemented using inexact alarms. Android may defer delivery under Doze or battery restrictions, and notifications are not visible if the user denies notification permission.
- Wake-word and unrestricted background microphone operation are intentionally not claimed; these require supported Android assistant/hotword APIs and device/OEM compatibility.
