package digital.vmstudio.myassistant

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import digital.vmstudio.myassistant.presentation.home.HomeScreen
import digital.vmstudio.myassistant.ui.theme.MyAssistantTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MyAssistantTheme {
                HomeScreen()
            }
        }
    }
}
