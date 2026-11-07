package digital.vmstudio.myassistant.domain.repository

import digital.vmstudio.myassistant.domain.model.AssistantChatMessage
import digital.vmstudio.myassistant.domain.model.AssistantNote
import digital.vmstudio.myassistant.domain.model.AssistantReminder
import digital.vmstudio.myassistant.domain.model.AssistantTask
import digital.vmstudio.myassistant.domain.model.MoneyTransaction
import kotlinx.coroutines.flow.Flow

/**
 * Application-facing contract for local user data.
 * AI features must call typed use cases built on this contract, never access SQL directly.
 */
interface AssistantRepository {
    fun observeTasks(): Flow<List<AssistantTask>>
    fun observeOpenTasks(): Flow<List<AssistantTask>>
    suspend fun getTask(id: String): AssistantTask?
    suspend fun addTask(task: AssistantTask)
    suspend fun updateTask(task: AssistantTask)
    suspend fun deleteTask(id: String)

    fun observeTransactions(): Flow<List<MoneyTransaction>>
    fun observeTransactionsBetween(startInclusive: Long, endExclusive: Long): Flow<List<MoneyTransaction>>
    fun observeExpenseTotal(startInclusive: Long, endExclusive: Long, currencyCode: String = "INR"): Flow<Long>
    fun observeIncomeTotal(startInclusive: Long, endExclusive: Long, currencyCode: String = "INR"): Flow<Long>
    suspend fun addTransaction(transaction: MoneyTransaction)
    suspend fun updateTransaction(transaction: MoneyTransaction)
    suspend fun deleteTransaction(id: String)

    fun observeReminders(): Flow<List<AssistantReminder>>
    fun observeEnabledReminders(): Flow<List<AssistantReminder>>
    suspend fun getReminder(id: String): AssistantReminder?
    suspend fun addReminder(reminder: AssistantReminder)
    suspend fun updateReminder(reminder: AssistantReminder)
    suspend fun deleteReminder(id: String)

    fun observeChatMessages(): Flow<List<AssistantChatMessage>>
    suspend fun addChatMessage(message: AssistantChatMessage)
    suspend fun clearChatHistory()

    fun observeNotes(): Flow<List<AssistantNote>>
    fun searchNotes(query: String): Flow<List<AssistantNote>>
    suspend fun getNote(id: String): AssistantNote?
    suspend fun addNote(note: AssistantNote)
    suspend fun updateNote(note: AssistantNote)
    suspend fun deleteNote(id: String)
}
