# Architecture

## Direction

The Android client follows Clean Architecture with MVVM and feature-oriented packages. Dependencies point inward: presentation depends on domain contracts; data implements domain contracts. Domain code must not depend on Android UI or network libraries.

## Planned package boundaries

- `core/`: shared design system, navigation, database, networking, security, date/time and result types.
- `domain/`: business models, repository interfaces and use cases.
- `data/`: Room entities/DAOs, repository implementations, gateway DTOs and mappers.
- `presentation/`: feature screens, state holders and UI models.
- `feature/`: assistant, tasks, finance, reminders, notes, projects, calendar and settings.

## Source of truth

Room is the primary source of truth for user-created records. AI responses are proposals until validated and committed by typed application use cases. Financial totals and reports are calculated by deterministic Kotlin code, never trusted from model-generated arithmetic.

## AI gateway

The gateway base URL is currently configured as `https://ai.vmstudio.digital`. Before implementing requests, inspect the actual OmniRoute API contract for authentication, model selection, streaming, tool calling, error formats and rate limits. Do not guess endpoint paths or JSON schemas. Never commit API tokens; prefer a controlled backend proxy if a private credential is required.

## Voice and Android restrictions

Push-to-talk is the first supported interaction. Hands-free activation is a later capability gated by Android-supported assistant/hotword APIs and device support. Background microphone use must remain user-visible and comply with foreground-service and permission rules. No hidden recording, root requirement or AccessibilityService workaround.

## Data safety

- Typed tool calls only; no arbitrary SQL, shell or code execution from the model.
- Confirm destructive actions before mutation.
- Verify persisted state before reporting an action as complete.
- Keep optional backup distinct from automatic two-way sync.
- Do not log transcripts, credentials or financial details unnecessarily.
