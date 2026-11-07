package digital.vmstudio.myassistant.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "chat_messages",
    indices = [Index(value = ["createdAtMillis"])]
)
data class ChatMessageEntity(
    @PrimaryKey val messageId: String,
    val role: String,
    val content: String,
    val createdAtMillis: Long
)
