package digital.vmstudio.myassistant.data.remote

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL

/**
 * OpenAI-compatible OmniRoute chat client. API credentials are supplied per request,
 * never stored in this class and never written to logs.
 */
class OmniRouteClient(
    private val baseUrl: String = DEFAULT_BASE_URL
) {
    suspend fun sendMessage(
        apiKey: String,
        model: String,
        messages: List<OmniRouteChatMessage>,
        includeTools: Boolean = true
    ): OmniRouteReply = withContext(Dispatchers.IO) {
        require(apiKey.isNotBlank()) { "Add your OmniRoute API key in Assistant settings." }
        require(model.isNotBlank()) { "Choose a model in Assistant settings." }
        require(messages.isNotEmpty()) { "Enter a message first." }

        val payload = JSONObject().apply {
            put("model", model)
            put("messages", JSONArray().also { array ->
                messages.forEach { message ->
                    val item = JSONObject()
                        .put("role", message.role)
                        .put("content", message.content)
                    message.toolCallId?.let { item.put("tool_call_id", it) }
                    message.toolCallsJson?.let { item.put("tool_calls", JSONArray(it)) }
                    array.put(item)
                }
            })
            put("stream", false)
            if (includeTools) {
                put("tools", AssistantToolDefinitions.asJsonArray())
                put("tool_choice", "auto")
            }
        }

        val connection = (URL(baseUrl.trimEnd('/') + "/v1/chat/completions")
            .openConnection() as HttpURLConnection).apply {
            requestMethod = "POST"
            connectTimeout = 20_000
            readTimeout = 90_000
            doOutput = true
            setRequestProperty("Authorization", "Bearer " + apiKey)
            setRequestProperty("Content-Type", "application/json")
            setRequestProperty("Accept", "application/json")
        }

        try {
            connection.outputStream.use { it.write(payload.toString().toByteArray(Charsets.UTF_8)) }
            val status = connection.responseCode
            val stream = if (status in 200..299) connection.inputStream else connection.errorStream
            val body = stream?.bufferedReader(Charsets.UTF_8)?.use { it.readText() }.orEmpty()
            if (status !in 200..299) {
                val detail = runCatching {
                    JSONObject(body).optJSONObject("error")?.optString("message").orEmpty()
                }.getOrDefault("")
                throw IOException(
                    when (status) {
                        400 -> "OmniRoute rejected the request (400). Check model and tool support."
                        401 -> "OmniRoute rejected the API key (401). Check Assistant settings."
                        403 -> "OmniRoute denied this request (403). Check key permissions."
                        404 -> "OmniRoute route or model was not found (404)."
                        429 -> "OmniRoute is rate-limiting requests (429). Try again shortly."
                        else -> "OmniRoute request failed (HTTP " + status + "). " + detail.take(180)
                    }
                )
            }

            val json = JSONObject(body)
            val choices = json.optJSONArray("choices")
                ?: throw IOException("OmniRoute returned an unexpected response.")
            val message = choices.optJSONObject(0)?.optJSONObject("message")
                ?: throw IOException("OmniRoute response did not contain a message.")
            val content = message.optString("content").takeUnless { it == "null" }?.trim().orEmpty()
            val rawCalls = message.optJSONArray("tool_calls")
            val calls = buildList {
                if (rawCalls != null) {
                    for (index in 0 until rawCalls.length()) {
                        val call = rawCalls.optJSONObject(index) ?: continue
                        val function = call.optJSONObject("function") ?: continue
                        val id = call.optString("id")
                        val name = function.optString("name")
                        val arguments = function.optString("arguments")
                        if (id.isNotBlank() && name.isNotBlank() && arguments.isNotBlank()) {
                            add(OmniRouteToolCall(
                                id = id,
                                name = name,
                                argumentsJson = arguments
                            ))
                        }
                    }
                }
            }
            if (content.isBlank() && calls.isEmpty()) {
                throw IOException("The selected model returned neither text nor a tool call.")
            }
            OmniRouteReply(
                text = content,
                model = json.optString("model").takeIf { it.isNotBlank() },
                toolCalls = calls,
                rawToolCallsJson = rawCalls?.toString()
            )
        } finally {
            connection.disconnect()
        }
    }

    companion object {
        const val DEFAULT_BASE_URL = "https://ai.vmstudio.digital"
        const val DEFAULT_MODEL = "auto"
    }
}
