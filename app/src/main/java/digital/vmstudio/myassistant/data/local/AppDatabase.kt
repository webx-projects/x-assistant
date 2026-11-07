package digital.vmstudio.myassistant.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import digital.vmstudio.myassistant.data.local.dao.FinancialTransactionDao
import digital.vmstudio.myassistant.data.local.dao.NoteDao
import digital.vmstudio.myassistant.data.local.dao.ReminderDao
import digital.vmstudio.myassistant.data.local.dao.TaskDao
import digital.vmstudio.myassistant.data.local.entity.FinancialTransactionEntity
import digital.vmstudio.myassistant.data.local.entity.NoteEntity
import digital.vmstudio.myassistant.data.local.entity.ReminderEntity
import digital.vmstudio.myassistant.data.local.entity.TaskEntity

@Database(
    entities = [
        TaskEntity::class,
        FinancialTransactionEntity::class,
        ReminderEntity::class,
        NoteEntity::class
    ],
    version = 1,
    exportSchema = true
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun taskDao(): TaskDao
    abstract fun financialTransactionDao(): FinancialTransactionDao
    abstract fun reminderDao(): ReminderDao
    abstract fun noteDao(): NoteDao
}
