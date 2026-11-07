package digital.vmstudio.myassistant.domain.model

data class AssistantChatMessage(
    val id: String,
    val role: String,
    val content: String,
    val createdAtMillis: Long
)
