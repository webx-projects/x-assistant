package digital.vmstudio.myassistant.reminders

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import digital.vmstudio.myassistant.AssistantApplication
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

/** Rebuilds alarms after reboot, manual clock changes or timezone changes. */
class ReminderRescheduleReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val pendingResult = goAsync()
        val app = context.applicationContext as AssistantApplication
        CoroutineScope(SupervisorJob() + Dispatchers.IO).launch {
            try {
                val scheduler = ReminderScheduler(context)
                app.assistantRepository.observeEnabledReminders().first()
                    .forEach(scheduler::schedule)
            } finally {
                pendingResult.finish()
            }
        }
    }
}
