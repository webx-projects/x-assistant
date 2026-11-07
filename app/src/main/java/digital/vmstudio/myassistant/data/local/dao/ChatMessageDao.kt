package digital.vmstudio.myassistant.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import digital.vmstudio.myassistant.data.local.entity.ChatMessageEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ChatMessageDao {
    @Query("SELECT * FROM chat_messages ORDER BY createdAtMillis ASC, messageId ASC")
    fun observeMessages(): Flow<List<ChatMessageEntity>>

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertMessage(message: ChatMessageEntity)

    @Query("DELETE FROM chat_messages")
    suspend fun deleteAllMessages()
}
