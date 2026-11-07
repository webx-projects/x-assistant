# Development Status

## Completed
- Native Android project foundation: Kotlin, Jetpack Compose and Material 3.
- Dashboard connected to Room-backed local data streams.
- Bottom navigation for Home, AI Assistant, Tasks, Money, Reminders and Notes.
- Task creation and completion/reopen flow.
- Income/expense entry with positive amount validation and paise-based storage.
- Reminder creation with local date/time parsing and future-time validation.
- Note creation and local listing.
- All-time income, expense and net totals calculated from stored integer minor units.
- Room database v1, DAOs, domain models, repository contract and Room repository.
- Initial finance summary unit tests.
- OmniRoute OpenAI-compatible non-streaming chat client targeting the configured host.
- Assistant conversation screen with model ID setting and API-key entry.
- API key encrypted with Android Keystore; app-private preferences store ciphertext only.
- Internet permission declared.
- Android CI workflow configured to install platform-tools and build debug APK plus unit tests.

## In progress / pending
- Verify the latest Android CI run and resolve any build or dependency errors.
- Verify the configured OmniRoute host and available models with a real API key.
- Add streamed responses, conversation persistence and model discovery.
- Add tests for Room DAOs, migrations, repository mapping, encrypted key storage and create-use-case validation.
- Add update/delete flows with explicit confirmation for destructive actions.
- Add date-range finance reports, categories and budgets.
- Implement notification scheduling for reminders, exact-alarm permission handling and reboot/timezone recovery.
- Implement push-to-talk speech recognition and text-to-speech.
- Implement typed AI tool routing with confirmation and database-state verification.
- Add secure optional backup, settings and release signing.

## Verification
- Initial CI attempt failed during SDK setup because the workflow requested the removed SDK `tools` package; Gradle did not start.
- A subsequent attempt passed SDK and Gradle setup but failed dependency resolution because Lifecycle 2.12.0 was selected before stable release. The version catalog was changed to Lifecycle 2.11.0.
- The latest UI and AI gateway source changes still require a successful Android CI run. No APK is claimed as built or verified.
- The OmniRoute request shape follows the public OpenAI-compatible protocol, but the deployed `ai.vmstudio.digital` instance and its model catalog have not yet been verified with a real API key.
