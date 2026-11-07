package digital.vmstudio.myassistant.domain.usecase

import digital.vmstudio.myassistant.domain.model.AssistantReminder
import digital.vmstudio.myassistant.domain.repository.AssistantRepository
import java.util.UUID

class CreateReminderUseCase(
    private val repository: AssistantRepository,
    private val now: () -> Long = System::currentTimeMillis,
    private val newId: () -> String = { UUID.randomUUID().toString() }
) {
    suspend operator fun invoke(
        title: String,
        scheduledAtMillis: Long,
        description: String = "",
        recurrenceRule: String? = null
    ): AssistantReminder {
        val normalizedTitle = title.trim()
        require(normalizedTitle.isNotEmpty()) { "Reminder title cannot be empty." }
        require(scheduledAtMillis > now()) { "Reminder time must be in the future." }
        val normalizedRule = recurrenceRule?.trim()?.takeIf(String::isNotEmpty)

        val timestamp = now()
        return AssistantReminder(
            id = newId(),
            title = normalizedTitle,
            description = description.trim(),
            scheduledAtMillis = scheduledAtMillis,
            recurrenceRule = normalizedRule,
            createdAtMillis = timestamp,
            updatedAtMillis = timestamp
        ).also(repository::addReminder)
    }
}
