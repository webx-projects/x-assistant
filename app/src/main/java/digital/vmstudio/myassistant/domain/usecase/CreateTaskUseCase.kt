package digital.vmstudio.myassistant.domain.usecase

import digital.vmstudio.myassistant.domain.model.AssistantTask
import digital.vmstudio.myassistant.domain.repository.AssistantRepository
import java.util.UUID

class CreateTaskUseCase(
    private val repository: AssistantRepository,
    private val now: () -> Long = System::currentTimeMillis,
    private val newId: () -> String = { UUID.randomUUID().toString() }
) {
    suspend operator fun invoke(
        title: String,
        details: String = "",
        dueAtMillis: Long? = null,
        priority: String = "NORMAL"
    ): AssistantTask {
        val normalizedTitle = title.trim()
        require(normalizedTitle.isNotEmpty()) { "Task title cannot be empty." }
        val normalizedPriority = priority.trim().uppercase()
        require(normalizedPriority in setOf("LOW", "NORMAL", "HIGH", "URGENT")) {
            "Unsupported task priority."
        }

        val timestamp = now()
        val task = AssistantTask(
            id = newId(),
            title = normalizedTitle,
            details = details.trim(),
            priority = normalizedPriority,
            dueAtMillis = dueAtMillis,
            createdAtMillis = timestamp,
            updatedAtMillis = timestamp
        )
        repository.addTask(task)
        return task
    }
}