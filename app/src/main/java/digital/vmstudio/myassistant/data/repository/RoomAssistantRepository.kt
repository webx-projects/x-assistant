package digital.vmstudio.myassistant.data.repository

import digital.vmstudio.myassistant.data.local.AppDatabase
import digital.vmstudio.myassistant.data.local.entity.ChatMessageEntity
import digital.vmstudio.myassistant.data.local.entity.FinancialTransactionEntity
import digital.vmstudio.myassistant.data.local.entity.NoteEntity
import digital.vmstudio.myassistant.data.local.entity.ReminderEntity
import digital.vmstudio.myassistant.data.local.entity.TaskEntity
import digital.vmstudio.myassistant.domain.model.AssistantChatMessage
import digital.vmstudio.myassistant.domain.model.AssistantNote
import digital.vmstudio.myassistant.domain.model.AssistantReminder
import digital.vmstudio.myassistant.domain.model.AssistantTask
import digital.vmstudio.myassistant.domain.model.MoneyTransaction
import digital.vmstudio.myassistant.domain.repository.AssistantRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class RoomAssistantRepository(
    private val database: AppDatabase
) : AssistantRepository {
    override fun observeTasks(): Flow<List<AssistantTask>> =
        database.taskDao().observeTasks().map { rows -> rows.map(TaskEntity::toDomain) }

    override fun observeOpenTasks(): Flow<List<AssistantTask>> =
        database.taskDao().observeOpenTasks().map { rows -> rows.map(TaskEntity::toDomain) }

    override suspend fun getTask(id: String): AssistantTask? =
        database.taskDao().getTask(id)?.toDomain()

    override suspend fun addTask(task: AssistantTask) =
        database.taskDao().insertTask(task.toEntity())

    override suspend fun updateTask(task: AssistantTask) =
        database.taskDao().updateTask(task.toEntity())

    override suspend fun deleteTask(id: String) =
        database.taskDao().deleteTask(id)

    override fun observeTransactions(): Flow<List<MoneyTransaction>> =
        database.financialTransactionDao().observeTransactions()
            .map { rows -> rows.map(FinancialTransactionEntity::toDomain) }

    override fun observeTransactionsBetween(
        startInclusive: Long,
        endExclusive: Long
    ): Flow<List<MoneyTransaction>> =
        database.financialTransactionDao()
            .observeTransactionsBetween(startInclusive, endExclusive)
            .map { rows -> rows.map(FinancialTransactionEntity::toDomain) }

    override fun observeExpenseTotal(
        startInclusive: Long,
        endExclusive: Long,
        currencyCode: String
    ): Flow<Long> = database.financialTransactionDao()
        .observeExpenseTotal(startInclusive, endExclusive, currencyCode)

    override fun observeIncomeTotal(
        startInclusive: Long,
        endExclusive: Long,
        currencyCode: String
    ): Flow<Long> = database.financialTransactionDao()
        .observeIncomeTotal(startInclusive, endExclusive, currencyCode)

    override suspend fun addTransaction(transaction: MoneyTransaction) =
        database.financialTransactionDao().insertTransaction(transaction.toEntity())

    override suspend fun updateTransaction(transaction: MoneyTransaction) =
        database.financialTransactionDao().updateTransaction(transaction.toEntity())

    override suspend fun deleteTransaction(id: String) =
        database.financialTransactionDao().deleteTransaction(id)

    override fun observeReminders(): Flow<List<AssistantReminder>> =
        database.reminderDao().observeReminders().map { rows -> rows.map(ReminderEntity::toDomain) }

    override fun observeEnabledReminders(): Flow<List<AssistantReminder>> =
        database.reminderDao().observeEnabledReminders()
            .map { rows -> rows.map(ReminderEntity::toDomain) }

    override suspend fun getReminder(id: String): AssistantReminder? =
        database.reminderDao().getReminder(id)?.toDomain()

    override suspend fun addReminder(reminder: AssistantReminder) =
        database.reminderDao().insertReminder(reminder.toEntity())

    override suspend fun updateReminder(reminder: AssistantReminder) =
        database.reminderDao().updateReminder(reminder.toEntity())

    override suspend fun deleteReminder(id: String) =
        database.reminderDao().deleteReminder(id)

    override fun observeChatMessages(): Flow<List<AssistantChatMessage>> =
        database.chatMessageDao().observeMessages().map { rows -> rows.map(ChatMessageEntity::toDomain) }

    override suspend fun addChatMessage(message: AssistantChatMessage) =
        database.chatMessageDao().insertMessage(message.toEntity())

    override suspend fun clearChatHistory() = database.chatMessageDao().deleteAllMessages()

    override fun observeNotes(): Flow<List<AssistantNote>> =
        database.noteDao().observeNotes().map { rows -> rows.map(NoteEntity::toDomain) }

    override fun searchNotes(query: String): Flow<List<AssistantNote>> =
        database.noteDao().searchNotes(query).map { rows -> rows.map(NoteEntity::toDomain) }

    override suspend fun getNote(id: String): AssistantNote? =
        database.noteDao().getNote(id)?.toDomain()

    override suspend fun addNote(note: AssistantNote) =
        database.noteDao().insertNote(note.toEntity())

    override suspend fun updateNote(note: AssistantNote) =
        database.noteDao().updateNote(note.toEntity())

    override suspend fun deleteNote(id: String) =
        database.noteDao().deleteNote(id)
}

private fun TaskEntity.toDomain() = AssistantTask(
    id = taskId,
    title = title,
    details = details,
    status = status,
    priority = priority,
    dueAtMillis = dueAtMillis,
    createdAtMillis = createdAtMillis,
    updatedAtMillis = updatedAtMillis
)

private fun AssistantTask.toEntity() = TaskEntity(
    taskId = id,
    title = title,
    details = details,
    status = status,
    priority = priority,
    dueAtMillis = dueAtMillis,
    createdAtMillis = createdAtMillis,
    updatedAtMillis = updatedAtMillis
)

private fun FinancialTransactionEntity.toDomain() = MoneyTransaction(
    id = transactionId,
    type = type,
    amountMinorUnits = amountMinorUnits,
    currencyCode = currencyCode,
    category = category,
    description = description,
    paymentMethod = paymentMethod,
    occurredAtMillis = occurredAtMillis,
    createdAtMillis = createdAtMillis
)

private fun MoneyTransaction.toEntity() = FinancialTransactionEntity(
    transactionId = id,
    type = type,
    amountMinorUnits = amountMinorUnits,
    currencyCode = currencyCode,
    category = category,
    description = description,
    paymentMethod = paymentMethod,
    occurredAtMillis = occurredAtMillis,
    createdAtMillis = createdAtMillis
)

private fun ReminderEntity.toDomain() = AssistantReminder(
    id = reminderId,
    title = title,
    description = description,
    scheduledAtMillis = scheduledAtMillis,
    recurrenceRule = recurrenceRule,
    isEnabled = isEnabled,
    createdAtMillis = createdAtMillis,
    updatedAtMillis = updatedAtMillis
)

private fun AssistantReminder.toEntity() = ReminderEntity(
    reminderId = id,
    title = title,
    description = description,
    scheduledAtMillis = scheduledAtMillis,
    recurrenceRule = recurrenceRule,
    isEnabled = isEnabled,
    createdAtMillis = createdAtMillis,
    updatedAtMillis = updatedAtMillis
)

private fun NoteEntity.toDomain() = AssistantNote(
    id = noteId,
    title = title,
    content = content,
    isPinned = isPinned,
    createdAtMillis = createdAtMillis,
    updatedAtMillis = updatedAtMillis
)

private fun AssistantNote.toEntity() = NoteEntity(
    noteId = id,
    title = title,
    content = content,
    isPinned = isPinned,
    createdAtMillis = createdAtMillis,
    updatedAtMillis = updatedAtMillis
)

private fun ChatMessageEntity.toDomain() = AssistantChatMessage(
    id = messageId, role = role, content = content, createdAtMillis = createdAtMillis
)

private fun AssistantChatMessage.toEntity() = ChatMessageEntity(
    messageId = id, role = role, content = content, createdAtMillis = createdAtMillis
)
