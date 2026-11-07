package digital.vmstudio.myassistant.reminders

import android.Manifest
import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.content.ContextCompat
import digital.vmstudio.myassistant.domain.model.AssistantReminder

/**
 * Schedules user-visible reminder alarms using Android's inexact idle-friendly API.
 * This intentionally avoids requesting the special exact-alarm access permission.
 */
class ReminderScheduler(context: Context) {
    private val appContext = context.applicationContext
    private val alarmManager = appContext.getSystemService(AlarmManager::class.java)

    fun notificationsAllowed(): Boolean = Build.VERSION.SDK_INT < 33 ||
        ContextCompat.checkSelfPermission(appContext, Manifest.permission.POST_NOTIFICATIONS) ==
        PackageManager.PERMISSION_GRANTED

    fun schedule(reminder: AssistantReminder) {
        if (!reminder.isEnabled || reminder.scheduledAtMillis <= System.currentTimeMillis()) {
            cancel(reminder.id)
            return
        }
        val pendingIntent = pendingIntent(reminder.id, reminder.title, reminder.description)
        alarmManager.setAndAllowWhileIdle(
            AlarmManager.RTC_WAKEUP,
            reminder.scheduledAtMillis,
            pendingIntent
        )
    }

    fun cancel(reminderId: String) {
        alarmManager.cancel(pendingIntent(reminderId, "", ""))
    }

    private fun pendingIntent(id: String, title: String, description: String): PendingIntent {
        val intent = Intent(appContext, ReminderAlarmReceiver::class.java).apply {
            action = ACTION_DELIVER
            data = android.net.Uri.parse("myassistant://reminder/$id")
            putExtra(EXTRA_ID, id)
            putExtra(EXTRA_TITLE, title)
            putExtra(EXTRA_DESCRIPTION, description)
        }
        return PendingIntent.getBroadcast(
            appContext,
            id.hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }

    companion object {
        const val ACTION_DELIVER = "digital.vmstudio.myassistant.action.DELIVER_REMINDER"
        const val EXTRA_ID = "reminder_id"
        const val EXTRA_TITLE = "reminder_title"
        const val EXTRA_DESCRIPTION = "reminder_description"
    }
}
