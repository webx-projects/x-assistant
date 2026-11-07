package digital.vmstudio.myassistant.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "reminders",
    indices = [
        Index(value = ["isEnabled", "scheduledAtMillis"]),
        Index(value = ["updatedAtMillis"])
    ]
)
data class ReminderEntity(
    @PrimaryKey val reminderId: String,
    val title: String,
    val description: String = "",
    val scheduledAtMillis: Long,
    /** Nullable recurrence rule; its format will be validated by the reminder domain layer. */
    val recurrenceRule: String? = null,
    val isEnabled: Boolean = true,
    val createdAtMillis: Long,
    val updatedAtMillis: Long
)
