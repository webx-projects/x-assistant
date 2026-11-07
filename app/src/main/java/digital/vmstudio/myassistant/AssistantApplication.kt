package digital.vmstudio.myassistant

import android.app.Application
import digital.vmstudio.myassistant.data.local.AppDatabase
import digital.vmstudio.myassistant.data.local.DatabaseProvider
import digital.vmstudio.myassistant.data.repository.RoomAssistantRepository
import digital.vmstudio.myassistant.domain.repository.AssistantRepository

class AssistantApplication : Application() {
    val database: AppDatabase by lazy(LazyThreadSafetyMode.SYNCHRONIZED) {
        DatabaseProvider.get(this)
    }

    val assistantRepository: AssistantRepository by lazy(LazyThreadSafetyMode.SYNCHRONIZED) {
        RoomAssistantRepository(database)
    }
}
