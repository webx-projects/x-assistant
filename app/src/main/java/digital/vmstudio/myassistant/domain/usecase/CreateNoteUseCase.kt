package digital.vmstudio.myassistant.domain.usecase

import digital.vmstudio.myassistant.domain.model.AssistantNote
import digital.vmstudio.myassistant.domain.repository.AssistantRepository
import java.util.UUID

class CreateNoteUseCase(
    private val repository: AssistantRepository,
    private val now: () -> Long = System::currentTimeMillis,
    private val newId: () -> String = { UUID.randomUUID().toString() }
) {
    suspend operator fun invoke(
        title: String,
        content: String,
        isPinned: Boolean = false
    ): AssistantNote {
        val normalizedTitle = title.trim()
        val normalizedContent = content.trim()
        require(normalizedTitle.isNotEmpty() || normalizedContent.isNotEmpty()) {
            "A note needs a title or some content."
        }

        val timestamp = now()
        return AssistantNote(
            id = newId(),
            title = normalizedTitle,
            content = normalizedContent,
            isPinned = isPinned,
            createdAtMillis = timestamp,
            updatedAtMillis = timestamp
        ).also(repository::addNote)
    }
}
