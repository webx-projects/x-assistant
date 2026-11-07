package digital.vmstudio.myassistant.presentation.assistant

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import digital.vmstudio.myassistant.data.local.secure.ApiKeyStore
import digital.vmstudio.myassistant.data.remote.OmniRouteClient
import kotlinx.coroutines.launch

private data class ChatLine(val role: String, val content: String)

@Composable
fun AssistantScreen() {
    val context = LocalContext.current
    val keyStore = remember { ApiKeyStore(context) }
    val client = remember { OmniRouteClient() }
    val scope = rememberCoroutineScope()
    val conversation = remember { mutableStateListOf<ChatLine>() }
    var draft by remember { mutableStateOf("") }
    var model by remember { mutableStateOf(OmniRouteClient.DEFAULT_MODEL) }
    var loading by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }
    var showSettings by remember { mutableStateOf(false) }
    var apiKey by remember { mutableStateOf("") }

    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Column(Modifier.weight(1f)) {
                Text("Ask anything", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                Text("OmniRoute · " + model, style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            OutlinedButton(onClick = {
                apiKey = runCatching { keyStore.read().orEmpty() }.getOrDefault("")
                showSettings = true
            }) { Text("Settings") }
        }

        if (conversation.isEmpty()) {
            Column(
                modifier = Modifier.weight(1f).fillMaxWidth().padding(top = 36.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text("Your personal assistant", style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.SemiBold)
                Text("Ask a question or describe what you want to do. Local task and finance actions will be connected through validated tools in a later milestone.",
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text("Examples", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                Text("• Explain a topic in simple Hindi")
                Text("• Help me plan my day")
                Text("• Summarise an idea")
            }
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f).fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(conversation) { line ->
                    val isUser = line.role == "user"
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(if (isUser) "You" else "Assistant",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Bold)
                        androidx.compose.material3.Surface(
                            shape = RoundedCornerShape(18.dp),
                            color = if (isUser) MaterialTheme.colorScheme.primaryContainer
                                else MaterialTheme.colorScheme.surfaceVariant
                        ) {
                            Text(line.content, Modifier.padding(14.dp))
                        }
                    }
                }
            }
        }

        error?.let { Text(it, color = MaterialTheme.colorScheme.error,
            style = MaterialTheme.typography.bodySmall) }
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedTextField(
                value = draft, onValueChange = { draft = it },
                modifier = Modifier.weight(1f),
                placeholder = { Text("Message your assistant…") },
                maxLines = 4
            )
            Button(
                enabled = draft.isNotBlank() && !loading,
                onClick = {
                    val userText = draft.trim()
                    draft = ""
                    error = null
                    conversation.add(ChatLine("user", userText))
                    loading = true
                    scope.launch {
                        try {
                            val key = keyStore.read()
                                ?: throw IllegalStateException("Add your OmniRoute API key in Assistant settings.")
                            val messages = conversation.map { it.role to it.content }
                            val reply = client.sendMessage(key, model, messages)
                            conversation.add(ChatLine("assistant", reply.text))
                        } catch (e: Exception) {
                            error = e.message ?: "The assistant request failed."
                        } finally {
                            loading = false
                        }
                    }
                }
            ) {
                if (loading) CircularProgressIndicator()
                else Text("Send")
            }
        }
    }

    if (showSettings) {
        AlertDialog(
            onDismissRequest = { showSettings = false },
            title = { Text("Assistant settings") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("Gateway: " + OmniRouteClient.DEFAULT_BASE_URL,
                        style = MaterialTheme.typography.bodySmall)
                    OutlinedTextField(
                        value = apiKey, onValueChange = { apiKey = it },
                        label = { Text("OmniRoute API key") },
                        modifier = Modifier.fillMaxWidth(),
                        visualTransformation = PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = model, onValueChange = { model = it },
                        label = { Text("Model ID (default: auto)") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    Text("The API key is encrypted with Android Keystore and stored on this device.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            },
            confirmButton = {
                Button(onClick = {
                    runCatching { keyStore.save(apiKey.trim()) }
                        .onFailure { error = it.message ?: "Could not save API key." }
                    showSettings = false
                }) { Text("Save") }
            },
            dismissButton = {
                Row {
                    TextButton(onClick = {
                        keyStore.clear()
                        apiKey = ""
                        error = "OmniRoute API key removed from this device."
                        showSettings = false
                    }) { Text("Remove key") }
                    TextButton(onClick = { showSettings = false }) { Text("Cancel") }
                }
            }
        )
    }
}
