package digital.vmstudio.myassistant.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import digital.vmstudio.myassistant.data.local.entity.ReminderEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ReminderDao {
    @Query("SELECT * FROM reminders ORDER BY scheduledAtMillis ASC")
    fun observeReminders(): Flow<List<ReminderEntity>>

    @Query("SELECT * FROM reminders WHERE isEnabled = 1 ORDER BY scheduledAtMillis ASC")
    fun observeEnabledReminders(): Flow<List<ReminderEntity>>

    @Query("SELECT * FROM reminders WHERE reminderId = :reminderId LIMIT 1")
    suspend fun getReminder(reminderId: String): ReminderEntity?

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertReminder(reminder: ReminderEntity)

    @Update
    suspend fun updateReminder(reminder: ReminderEntity)

    @Query("DELETE FROM reminders WHERE reminderId = :reminderId")
    suspend fun deleteReminder(reminderId: String)
}
