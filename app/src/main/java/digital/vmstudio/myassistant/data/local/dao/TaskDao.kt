package digital.vmstudio.myassistant.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import digital.vmstudio.myassistant.data.local.entity.TaskEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface TaskDao {
    @Query("SELECT * FROM tasks ORDER BY CASE WHEN dueAtMillis IS NULL THEN 1 ELSE 0 END, dueAtMillis ASC, updatedAtMillis DESC")
    fun observeTasks(): Flow<List<TaskEntity>>

    @Query("SELECT * FROM tasks WHERE status != 'COMPLETED' AND status != 'ARCHIVED' ORDER BY CASE WHEN dueAtMillis IS NULL THEN 1 ELSE 0 END, dueAtMillis ASC")
    fun observeOpenTasks(): Flow<List<TaskEntity>>

    @Query("SELECT * FROM tasks WHERE taskId = :taskId LIMIT 1")
    suspend fun getTask(taskId: String): TaskEntity?

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertTask(task: TaskEntity)

    @Update
    suspend fun updateTask(task: TaskEntity)

    @Query("DELETE FROM tasks WHERE taskId = :taskId")
    suspend fun deleteTask(taskId: String)
}
