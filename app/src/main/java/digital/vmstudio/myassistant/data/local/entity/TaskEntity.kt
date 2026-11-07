package digital.vmstudio.myassistant.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "tasks",
    indices = [
        Index(value = ["status", "dueAtMillis"]),
        Index(value = ["updatedAtMillis"])
    ]
)
data class TaskEntity(
    @PrimaryKey val taskId: String,
    val title: String,
    val details: String = "",
    /** OPEN, IN_PROGRESS, COMPLETED, or ARCHIVED. */
    val status: String = "OPEN",
    /** LOW, NORMAL, HIGH, or URGENT. */
    val priority: String = "NORMAL",
    val dueAtMillis: Long? = null,
    val createdAtMillis: Long,
    val updatedAtMillis: Long
)
