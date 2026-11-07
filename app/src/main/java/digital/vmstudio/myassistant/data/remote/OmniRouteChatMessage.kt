package digital.vmstudio.myassistant.data.remote

data class OmniRouteChatMessage(
    val role: String,
    val content: String,
    val toolCallId: String? = null,
    /** Raw JSON array from the gateway assistant message; generated only from a parsed response. */
    val toolCallsJson: String? = null
)

data class OmniRouteToolCall(
    val id: String,
    val name: String,
    val argumentsJson: String
)

data class OmniRouteReply(
    val text: String,
    val model: String? = null,
    val toolCalls: List<OmniRouteToolCall> = emptyList(),
    val rawToolCallsJson: String? = null
)
