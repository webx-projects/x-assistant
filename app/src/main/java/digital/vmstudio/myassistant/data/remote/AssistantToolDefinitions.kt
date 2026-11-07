package digital.vmstudio.myassistant.data.remote

import org.json.JSONArray
import org.json.JSONObject

/**
 * Strict, app-owned tool allowlist. The model can request only these named operations;
 * the Android client validates and executes them through repository/use-case methods.
 */
object AssistantToolDefinitions {
    fun asJsonArray(): JSONArray = JSONArray().apply {
        put(function(
            "create_task",
            "Create a task in the user's local task list.",
            objectSchema(
                properties = mapOf(
                    "title" to stringProperty("Task title"),
                    "details" to stringProperty("Optional task details"),
                    "priority" to enumProperty("Priority", listOf("LOW", "NORMAL", "HIGH", "URGENT"))
                ),
                required = listOf("title")
            )
        ))
        put(function(
            "create_expense",
            "Record an expense. Amount is expressed in the user's currency units, not paise.",
            objectSchema(
                properties = mapOf(
                    "amount" to numberProperty("Positive amount in currency units, e.g. 450.50"),
                    "category" to stringProperty("Expense category"),
                    "description" to stringProperty("Optional short description")
                ),
                required = listOf("amount", "category")
            )
        ))
        put(function(
            "create_income",
            "Record income. Amount is expressed in the user's currency units, not paise.",
            objectSchema(
                properties = mapOf(
                    "amount" to numberProperty("Positive amount in currency units"),
                    "category" to stringProperty("Income category"),
                    "description" to stringProperty("Optional short description")
                ),
                required = listOf("amount", "category")
            )
        ))
        put(function(
            "create_reminder",
            "Create a reminder. local_datetime must be ISO-8601 with an explicit UTC offset.",
            objectSchema(
                properties = mapOf(
                    "title" to stringProperty("Reminder title"),
                    "local_datetime" to stringProperty("ISO-8601 datetime including offset, e.g. 2026-12-01T10:00:00+05:30"),
                    "details" to stringProperty("Optional reminder details")
                ),
                required = listOf("title", "local_datetime")
            )
        ))
        put(function(
            "create_note",
            "Save a note in the user's local notes.",
            objectSchema(
                properties = mapOf(
                    "title" to stringProperty("Optional note title"),
                    "content" to stringProperty("Note content")
                ),
                required = listOf("content")
            )
        ))
        put(function(
            "list_open_tasks",
            "List the user's currently open and in-progress tasks.",
            objectSchema(emptyMap(), emptyList())
        ))
        put(function(
            "get_finance_summary",
            "Calculate income, expenses and net amount for a date range. end_date is exclusive. Dates use YYYY-MM-DD in the device timezone.",
            objectSchema(
                properties = mapOf(
                    "start_date" to stringProperty("Inclusive start date YYYY-MM-DD"),
                    "end_date" to stringProperty("Exclusive end date YYYY-MM-DD")
                ),
                required = listOf("start_date", "end_date")
            )
        ))
    }

    private fun function(name: String, description: String, parameters: JSONObject) =
        JSONObject().put("type", "function").put(
            "function",
            JSONObject()
                .put("name", name)
                .put("description", description)
                .put("parameters", parameters)
                .put("strict", true)
        )

    private fun objectSchema(
        properties: Map<String, JSONObject>,
        required: List<String>
    ) = JSONObject()
        .put("type", "object")
        .put("properties", JSONObject().apply {
            properties.forEach { (key, value) -> put(key, value) }
        })
        .put("required", JSONArray(required))
        .put("additionalProperties", false)

    private fun stringProperty(description: String) =
        JSONObject().put("type", "string").put("description", description)

    private fun numberProperty(description: String) =
        JSONObject().put("type", "number").put("description", description)

    private fun enumProperty(description: String, values: List<String>) =
        JSONObject().put("type", "string").put("description", description)
            .put("enum", JSONArray(values))
}
