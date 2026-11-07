package digital.vmstudio.myassistant.domain.usecase

import digital.vmstudio.myassistant.domain.repository.AssistantRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.first
import org.json.JSONArray
import org.json.JSONObject
import java.math.BigDecimal
import java.math.RoundingMode
import java.time.LocalDate
import java.time.OffsetDateTime
import java.time.ZoneId

/**
 * Executes only the app-owned tool allowlist. Unknown names are rejected; the model
 * never receives a database handle, SQL interface, reflection or arbitrary code runner.
 */
class AssistantToolExecutor(
    private val repository: AssistantRepository,
    private val zoneId: ZoneId = ZoneId.systemDefault()
) {
    suspend fun execute(toolName: String, argumentsJson: String): String {
        return try {
            val args = JSONObject(argumentsJson)
            when (toolName) {
                "create_task" -> createTask(args)
                "create_expense" -> createTransaction(args, "EXPENSE")
                "create_income" -> createTransaction(args, "INCOME")
                "create_reminder" -> createReminder(args)
                "create_note" -> createNote(args)
                "list_open_tasks" -> listOpenTasks()
                "get_finance_summary" -> financeSummary(args)
                else -> errorJson("Unsupported tool: " + toolName)
            }
        } catch (cancelled: CancellationException) {
            throw cancelled
        } catch (e: Exception) {
            errorJson(e.message ?: "The requested action could not be completed.")
        }
    }

    private suspend fun createTask(args: JSONObject): String {
        val task = CreateTaskUseCase(repository)(
            title = args.getString("title"),
            details = args.optString("details"),
            priority = args.optString("priority", "NORMAL")
        )
        return JSONObject()
            .put("ok", true)
            .put("message", "Task saved.")
            .put("task_id", task.id)
            .put("title", task.title)
            .toString()
    }

    private suspend fun createTransaction(args: JSONObject, type: String): String {
        val amountMinor = BigDecimal(args.get("amount").toString())
            .setScale(2, RoundingMode.UNNECESSARY)
            .movePointRight(2)
            .longValueExact()
        val transaction = CreateTransactionUseCase(repository)(
            type = type,
            amountMinorUnits = amountMinor,
            category = args.getString("category"),
            description = args.optString("description"),
            currencyCode = "INR"
        )
        return JSONObject()
            .put("ok", true)
            .put("message", if (type == "EXPENSE") "Expense saved." else "Income saved.")
            .put("transaction_id", transaction.id)
            .put("type", transaction.type)
            .put("amount_minor_units", transaction.amountMinorUnits)
            .put("currency", transaction.currencyCode)
            .put("category", transaction.category)
            .toString()
    }

    private suspend fun createReminder(args: JSONObject): String {
        val scheduledAt = OffsetDateTime.parse(args.getString("local_datetime"))
            .toInstant().toEpochMilli()
        val reminder = CreateReminderUseCase(repository)(
            title = args.getString("title"),
            scheduledAtMillis = scheduledAt,
            description = args.optString("details")
        )
        return JSONObject()
            .put("ok", true)
            .put("message", "Reminder saved. Notification scheduling will be added in the reminder service milestone.")
            .put("reminder_id", reminder.id)
            .put("scheduled_at_epoch_millis", reminder.scheduledAtMillis)
            .toString()
    }

    private suspend fun createNote(args: JSONObject): String {
        val note = CreateNoteUseCase(repository)(
            title = args.optString("title"),
            content = args.getString("content")
        )
        return JSONObject()
            .put("ok", true)
            .put("message", "Note saved.")
            .put("note_id", note.id)
            .toString()
    }

    private suspend fun listOpenTasks(): String {
        val rows = repository.observeOpenTasks().first()
        val tasks = JSONArray()
        rows.forEach { task ->
            tasks.put(JSONObject()
                .put("id", task.id)
                .put("title", task.title)
                .put("details", task.details)
                .put("priority", task.priority)
                .put("due_at_epoch_millis", task.dueAtMillis ?: JSONObject.NULL))
        }
        return JSONObject().put("ok", true).put("tasks", tasks).toString()
    }

    private suspend fun financeSummary(args: JSONObject): String {
        val startDate = LocalDate.parse(args.getString("start_date"))
        val endDate = LocalDate.parse(args.getString("end_date"))
        require(startDate.isBefore(endDate)) { "The finance period must have an end date after its start date." }
        val start = startDate.atStartOfDay(zoneId).toInstant().toEpochMilli()
        val end = endDate.atStartOfDay(zoneId).toInstant().toEpochMilli()
        val income = repository.observeIncomeTotal(start, end, "INR").first()
        val expenses = repository.observeExpenseTotal(start, end, "INR").first()
        return JSONObject()
            .put("ok", true)
            .put("currency", "INR")
            .put("start_date_inclusive", startDate.toString())
            .put("end_date_exclusive", endDate.toString())
            .put("income_minor_units", income)
            .put("expense_minor_units", expenses)
            .put("net_minor_units", income - expenses)
            .toString()
    }

    private fun errorJson(message: String): String =
        JSONObject().put("ok", false).put("error", message.take(240)).toString()
}
