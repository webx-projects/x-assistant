package digital.vmstudio.myassistant.domain.model

data class AssistantTask(
    val id: String,
    val title: String,
    val details: String = "",
    val status: String = "OPEN",
    val priority: String = "NORMAL",
    val dueAtMillis: Long? = null,
    val createdAtMillis: Long,
    val updatedAtMillis: Long
)

data class MoneyTransaction(
    val id: String,
    val type: String,
    val amountMinorUnits: Long,
    val currencyCode: String = "INR",
    val category: String,
    val description: String = "",
    val paymentMethod: String? = null,
    val occurredAtMillis: Long,
    val createdAtMillis: Long
)

data class AssistantReminder(
    val id: String,
    val title: String,
    val description: String = "",
    val scheduledAtMillis: Long,
    val recurrenceRule: String? = null,
    val isEnabled: Boolean = true,
    val createdAtMillis: Long,
    val updatedAtMillis: Long
)

data class AssistantNote(
    val id: String,
    val title: String,
    val content: String,
    val isPinned: Boolean = false,
    val createdAtMillis: Long,
    val updatedAtMillis: Long
)
