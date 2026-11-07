package digital.vmstudio.myassistant.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "notes",
    indices = [Index(value = ["updatedAtMillis"]), Index(value = ["isPinned", "updatedAtMillis"])]
)
data class NoteEntity(
    @PrimaryKey val noteId: String,
    val title: String,
    val content: String,
    val isPinned: Boolean = false,
    val createdAtMillis: Long,
    val updatedAtMillis: Long
)
