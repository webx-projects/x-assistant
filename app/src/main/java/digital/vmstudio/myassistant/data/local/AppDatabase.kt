package digital.vmstudio.myassistant.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import digital.vmstudio.myassistant.data.local.dao.ChatMessageDao
import digital.vmstudio.myassistant.data.local.dao.FinancialTransactionDao
import digital.vmstudio.myassistant.data.local.dao.NoteDao
import digital.vmstudio.myassistant.data.local.dao.ReminderDao
import digital.vmstudio.myassistant.data.local.dao.TaskDao
import digital.vmstudio.myassistant.data.local.entity.ChatMessageEntity
import digital.vmstudio.myassistant.data.local.entity.FinancialTransactionEntity
import digital.vmstudio.myassistant.data.local.entity.NoteEntity
import digital.vmstudio.myassistant.data.local.entity.ReminderEntity
import digital.vmstudio.myassistant.data.local.entity.TaskEntity

@Database(
    entities = [
        TaskEntity::class,
        FinancialTransactionEntity::class,
        ReminderEntity::class,
        NoteEntity::class,
        ChatMessageEntity::class
    ],
    version = 2,
    exportSchema = true
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun chatMessageDao(): ChatMessageDao
    abstract fun taskDao(): TaskDao
    abstract fun financialTransactionDao(): FinancialTransactionDao
    abstract fun reminderDao(): ReminderDao
    abstract fun noteDao(): NoteDao

    companion object {
        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("CREATE TABLE IF NOT EXISTS chat_messages (messageId TEXT NOT NULL PRIMARY KEY, role TEXT NOT NULL, content TEXT NOT NULL, createdAtMillis INTEGER NOT NULL)")
                database.execSQL("CREATE INDEX IF NOT EXISTS index_chat_messages_createdAtMillis ON chat_messages (createdAtMillis)")
            }
        }
    }
}
