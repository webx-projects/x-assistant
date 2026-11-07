package digital.vmstudio.myassistant.data.local

import android.content.Context
import androidx.room.Room

/**
 * Owns the process-wide Room instance. The database remains the source of truth;
 * repositories and use cases should access it through the application container.
 */
object DatabaseProvider {
    @Volatile
    private var instance: AppDatabase? = null

    fun get(context: Context): AppDatabase =
        instance ?: synchronized(this) {
            instance ?: Room.databaseBuilder(
                context.applicationContext,
                AppDatabase::class.java,
                "my_ai_assistant.db"
            ).build().also { instance = it }
        }
}
