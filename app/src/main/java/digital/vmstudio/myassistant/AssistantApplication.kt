package digital.vmstudio.myassistant

import android.app.Application
import digital.vmstudio.myassistant.data.local.AppDatabase
import digital.vmstudio.myassistant.data.local.DatabaseProvider

class AssistantApplication : Application() {
    val database: AppDatabase by lazy(LazyThreadSafetyMode.SYNCHRONIZED) {
        DatabaseProvider.get(this)
    }
}
