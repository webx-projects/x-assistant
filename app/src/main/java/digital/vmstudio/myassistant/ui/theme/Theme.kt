package digital.vmstudio.myassistant.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val AssistantLightColors = lightColorScheme(
    primary = Color(0xFFB65C18),
    onPrimary = Color(0xFFFFFFFF),
    primaryContainer = Color(0xFFFFDCC5),
    onPrimaryContainer = Color(0xFF3B1600),
    secondary = Color(0xFF755846),
    background = Color(0xFFFFF8F4),
    surface = Color(0xFFFFF8F4),
    surfaceVariant = Color(0xFFF3E5DC)
)

private val AssistantDarkColors = darkColorScheme(
    primary = Color(0xFFFFB783),
    onPrimary = Color(0xFF612C00),
    primaryContainer = Color(0xFF813F08),
    onPrimaryContainer = Color(0xFFFFDCC5),
    secondary = Color(0xFFE6C1A8),
    background = Color(0xFF17120F),
    surface = Color(0xFF17120F),
    surfaceVariant = Color(0xFF342820)
)

@Composable
fun MyAssistantTheme(
    darkTheme: Boolean = false,
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = if (darkTheme) AssistantDarkColors else AssistantLightColors,
        typography = androidx.compose.material3.Typography(),
        content = content
    )
}
