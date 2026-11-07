package digital.vmstudio.myassistant.presentation.home

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.core.content.ContextCompat
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import digital.vmstudio.myassistant.AssistantApplication
import digital.vmstudio.myassistant.domain.model.*
import digital.vmstudio.myassistant.reminders.ReminderScheduler
import kotlinx.coroutines.launch
import java.math.BigDecimal
import java.math.RoundingMode
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.UUID

private enum class AssistantTab(val label: String, val symbol: String) {
    HOME("Home", "⌂"), ASSISTANT("AI", "✦"), TASKS("Tasks", "✓"), FINANCE("Money", "₹"),
    MORE("More", "•••"), REMINDERS("Reminders", "◷"), NOTES("Notes", "▤")
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen() {
    val context = LocalContext.current
    val repository = (context.applicationContext as AssistantApplication).assistantRepository
    val scope = rememberCoroutineScope()
    val reminderScheduler = remember(context) { ReminderScheduler(context) }
    val notificationPermission = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { }
    var notificationPermissionRequested by remember { mutableStateOf(false) }
    val tasks by repository.observeTasks().collectAsState(initial = emptyList())
    val transactions by repository.observeTransactions().collectAsState(initial = emptyList())
    val reminders by repository.observeReminders().collectAsState(initial = emptyList())
    val notes by repository.observeNotes().collectAsState(initial = emptyList())
    var tab by remember { mutableStateOf(AssistantTab.HOME) }
    var dialog by remember { mutableStateOf<AssistantTab?>(null) }
    var error by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(tab) {
        if (tab == AssistantTab.REMINDERS && !notificationPermissionRequested && Build.VERSION.SDK_INT >= 33) {
            notificationPermissionRequested = true
            if (ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                notificationPermission.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(title = {
                Column {
                    Text("MY AI ASSISTANT", style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                    Text(tab.label, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.SemiBold)
                }
            })
        },
        bottomBar = {
            NavigationBar {
                listOf(AssistantTab.HOME, AssistantTab.ASSISTANT, AssistantTab.TASKS, AssistantTab.FINANCE, AssistantTab.MORE).forEach { item ->
                    NavigationBarItem(
                        selected = tab == item || (item == AssistantTab.MORE && (tab == AssistantTab.REMINDERS || tab == AssistantTab.NOTES)),
                        onClick = { tab = item; error = null },
                        icon = { Text(item.symbol) },
                        label = { Text(item.label) }
                    )
                }
            }
        },
        floatingActionButton = {
            if (tab != AssistantTab.HOME && tab != AssistantTab.MORE) FloatingActionButton(
                onClick = { dialog = tab; error = null }
            ) { Text("+", style = MaterialTheme.typography.headlineMedium) }
        }
    ) { padding ->
        Column(
            Modifier.fillMaxSize().padding(padding).padding(horizontal = 18.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            error?.let { Text(it, color = MaterialTheme.colorScheme.error) }
            when (tab) {
                AssistantTab.HOME -> DashboardContent(tasks, transactions, reminders) { tab = it }
                AssistantTab.ASSISTANT -> digital.vmstudio.myassistant.presentation.assistant.AssistantScreen()
                AssistantTab.TASKS -> TaskList(tasks) { task ->
                    scope.launch {
                        repository.updateTask(task.copy(
                            status = if (task.status == "COMPLETED") "OPEN" else "COMPLETED",
                            updatedAtMillis = System.currentTimeMillis()
                        ))
                    }
                }
                AssistantTab.FINANCE -> FinanceList(transactions)
                AssistantTab.REMINDERS -> ReminderList(reminders)
                AssistantTab.NOTES -> NoteList(notes)
                AssistantTab.MORE -> MoreContent { tab = it }
            }
        }
    }

    when (dialog) {
        AssistantTab.TASKS -> CreateDialog(
            "New task", listOf("Task title", "Details (optional)"),
            onDismiss = { dialog = null }
        ) { values ->
            scope.launch {
                try {
                    val title = values[0].trim()
                    require(title.isNotEmpty()) { "Enter a task title." }
                    val now = System.currentTimeMillis()
                    repository.addTask(AssistantTask(
                        UUID.randomUUID().toString(), title, values[1].trim(),
                        createdAtMillis = now, updatedAtMillis = now
                    ))
                } catch (e: Exception) { error = e.message ?: "Could not save task." }
                dialog = null
            }
        }
        AssistantTab.FINANCE -> CreateDialog(
            "Add transaction",
            listOf("Amount in ₹", "Category", "Description (optional)", "Type: EXPENSE or INCOME"),
            listOf("", "Other", "", "EXPENSE"),
            onDismiss = { dialog = null }
        ) { values ->
            scope.launch {
                try {
                    val minor = BigDecimal(values[0].trim()).setScale(2, RoundingMode.UNNECESSARY)
                        .movePointRight(2).longValueExact()
                    require(minor > 0L) { "Enter an amount greater than zero." }
                    val type = values[3].trim().uppercase()
                    require(type == "EXPENSE" || type == "INCOME") { "Type must be EXPENSE or INCOME." }
                    val now = System.currentTimeMillis()
                    repository.addTransaction(MoneyTransaction(
                        id = UUID.randomUUID().toString(), type = type, amountMinorUnits = minor,
                        category = values[1].trim().ifEmpty { "Other" },
                        description = values[2].trim(), occurredAtMillis = now, createdAtMillis = now
                    ))
                } catch (e: Exception) { error = e.message ?: "Could not save transaction." }
                dialog = null
            }
        }
        AssistantTab.REMINDERS -> CreateDialog(
            "New reminder",
            listOf("Reminder title", "Date & time (yyyy-MM-dd HH:mm)", "Details (optional)"),
            listOf("", LocalDateTime.now().plusHours(1).format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")), ""),
            onDismiss = { dialog = null }
        ) { values ->
            scope.launch {
                try {
                    val title = values[0].trim()
                    require(title.isNotEmpty()) { "Enter a reminder title." }
                    val local = LocalDateTime.parse(values[1].trim(),
                        DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"))
                    val scheduled = local.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
                    require(scheduled > System.currentTimeMillis()) { "Choose a future date and time." }
                    val now = System.currentTimeMillis()
                    val reminder = AssistantReminder(
                        id = UUID.randomUUID().toString(), title = title,
                        description = values[2].trim(), scheduledAtMillis = scheduled,
                        createdAtMillis = now, updatedAtMillis = now
                    )
                    repository.addReminder(reminder)
                    reminderScheduler.schedule(reminder)
                } catch (e: Exception) { error = e.message ?: "Could not save reminder." }
                dialog = null
            }
        }
        AssistantTab.NOTES -> CreateDialog(
            "New note", listOf("Title (optional)", "Write your note"),
            onDismiss = { dialog = null }
        ) { values ->
            scope.launch {
                try {
                    require(values.any { it.isNotBlank() }) { "Write a title or note." }
                    val now = System.currentTimeMillis()
                    repository.addNote(AssistantNote(
                        id = UUID.randomUUID().toString(), title = values[0].trim(),
                        content = values[1].trim(), createdAtMillis = now, updatedAtMillis = now
                    ))
                } catch (e: Exception) { error = e.message ?: "Could not save note." }
                dialog = null
            }
        }
        else -> Unit
    }
}

@Composable
private fun DashboardContent(
    tasks: List<AssistantTask>,
    transactions: List<MoneyTransaction>,
    reminders: List<AssistantReminder>,
    onOpen: (AssistantTab) -> Unit
) {
    val income = transactions.filter { it.type == "INCOME" }.sumOf { it.amountMinorUnits }
    val expenses = transactions.filter { it.type == "EXPENSE" }.sumOf { it.amountMinorUnits }
    val openTasks = tasks.count { it.status != "COMPLETED" && it.status != "ARCHIVED" }
    val upcoming = reminders.filter { it.isEnabled && it.scheduledAtMillis >= System.currentTimeMillis() }
        .sortedBy { it.scheduledAtMillis }.take(3)

    LazyColumn(Modifier.fillMaxSize(), verticalArrangement = Arrangement.spacedBy(14.dp)) {
        item {
            Column(verticalArrangement = Arrangement.spacedBy(5.dp)) {
                Text("Good day.", style = MaterialTheme.typography.headlineLarge, fontWeight = FontWeight.Bold)
                Text("Your day, organised in one place.", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
        item {
            Card(
                modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(26.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
            ) {
                Column(Modifier.padding(22.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("✦", style = MaterialTheme.typography.headlineLarge,
                        color = MaterialTheme.colorScheme.onPrimaryContainer)
                    Text("Your assistant is ready", style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onPrimaryContainer)
                    Text("Manage your tasks, money, reminders and notes here.",
                        color = MaterialTheme.colorScheme.onPrimaryContainer)
                    Text("Voice AI is the next development milestone.",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer)
                }
            }
        }
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                MetricCard("Open tasks", openTasks.toString(), Modifier.weight(1f))
                MetricCard("Upcoming", upcoming.size.toString(), Modifier.weight(1f))
            }
        }
        item {
            Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(20.dp)) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text("All-time money overview", fontWeight = FontWeight.SemiBold)
                    Text("Income  ₹" + formatMinor(income))
                    Text("Expenses  ₹" + formatMinor(expenses))
                    Text("Net  ₹" + formatMinor(income - expenses), fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary)
                    TextButton(onClick = { onOpen(AssistantTab.FINANCE) }) { Text("Open finance") }
                }
            }
        }
        item {
            DashboardAction("Ask your AI assistant", "Chat through your OmniRoute gateway", Modifier.fillMaxWidth()) {
                onOpen(AssistantTab.ASSISTANT)
            }
        }
        item { Text("Your workspace", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold) }
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                DashboardAction("Tasks", "Plan your day", Modifier.weight(1f)) { onOpen(AssistantTab.TASKS) }
                DashboardAction("Money", "Track spending", Modifier.weight(1f)) { onOpen(AssistantTab.FINANCE) }
            }
        }
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                DashboardAction("Reminders", "Remember what matters", Modifier.weight(1f)) { onOpen(AssistantTab.REMINDERS) }
                DashboardAction("Notes", "Capture thoughts", Modifier.weight(1f)) { onOpen(AssistantTab.NOTES) }
            }
        }
        if (upcoming.isNotEmpty()) {
            item { Text("Coming up", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold) }
            items(upcoming, key = { it.id }) { reminder ->
                RecordCard(reminder.title, formatDateTime(reminder.scheduledAtMillis), "Reminder")
            }
        }
        item { Spacer(Modifier.height(8.dp)) }
    }
}

@Composable
private fun MoreContent(onOpen: (AssistantTab) -> Unit) {
    Column(Modifier.fillMaxWidth().padding(top = 12.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text("More tools", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        DashboardAction("Reminders", "Manage upcoming reminders", Modifier.fillMaxWidth()) { onOpen(AssistantTab.REMINDERS) }
        DashboardAction("Notes", "Browse your saved notes", Modifier.fillMaxWidth()) { onOpen(AssistantTab.NOTES) }
    }
}

@Composable
private fun TaskList(tasks: List<AssistantTask>, onToggle: (AssistantTask) -> Unit) {
    if (tasks.isEmpty()) EmptyState("No tasks yet", "Tap + to add your first task.")
    else LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        items(tasks, key = { it.id }) { task ->
            Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(18.dp)) {
                Row(Modifier.padding(16.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(if (task.status == "COMPLETED") "✓" else "○",
                        color = MaterialTheme.colorScheme.primary)
                    Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(task.title, fontWeight = FontWeight.SemiBold)
                        if (task.details.isNotBlank()) Text(task.details,
                            color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(task.priority + " · " + task.status,
                            style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
                    }
                    TextButton(onClick = { onToggle(task) }) {
                        Text(if (task.status == "COMPLETED") "Reopen" else "Done")
                    }
                }
            }
        }
    }
}

@Composable
private fun FinanceList(transactions: List<MoneyTransaction>) {
    if (transactions.isEmpty()) EmptyState("No transactions yet", "Tap + to record income or an expense.")
    else LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        item {
            val income = transactions.filter { it.type == "INCOME" }.sumOf { it.amountMinorUnits }
            val expenses = transactions.filter { it.type == "EXPENSE" }.sumOf { it.amountMinorUnits }
            Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(18.dp)) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text("All-time summary", fontWeight = FontWeight.SemiBold)
                    Text("Income  ₹" + formatMinor(income))
                    Text("Expenses  ₹" + formatMinor(expenses))
                    Text("Net  ₹" + formatMinor(income - expenses), fontWeight = FontWeight.Bold)
                }
            }
        }
        items(transactions, key = { it.id }) { transaction ->
            val sign = if (transaction.type == "INCOME") "+" else "−"
            RecordCard(transaction.category,
                (transaction.description.ifBlank { transaction.type }) + " · " + formatDateTime(transaction.occurredAtMillis),
                sign + "₹" + formatMinor(transaction.amountMinorUnits))
        }
    }
}

@Composable
private fun ReminderList(reminders: List<AssistantReminder>) {
    if (reminders.isEmpty()) EmptyState("No reminders yet", "Tap + to create a reminder.")
    else LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        items(reminders, key = { it.id }) { reminder ->
            RecordCard(reminder.title, formatDateTime(reminder.scheduledAtMillis) + " · " + reminder.description,
                if (reminder.isEnabled) "Active" else "Paused")
        }
    }
}

@Composable
private fun NoteList(notes: List<AssistantNote>) {
    if (notes.isEmpty()) EmptyState("No notes yet", "Tap + to save your first note.")
    else LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        items(notes, key = { it.id }) { note ->
            RecordCard(note.title.ifBlank { "Untitled note" }, note.content,
                if (note.isPinned) "Pinned" else "Note")
        }
    }
}

@Composable
private fun CreateDialog(
    title: String,
    fields: List<String>,
    initialValues: List<String> = emptyList(),
    onDismiss: () -> Unit,
    onSave: (List<String>) -> Unit
) {
    val values = remember(title) {
        fields.mapIndexed { index, _ -> initialValues.getOrNull(index).orEmpty() }.toMutableStateList()
    }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(9.dp)) {
                fields.forEachIndexed { index, label ->
                    OutlinedTextField(
                        value = values[index], onValueChange = { values[index] = it },
                        label = { Text(label) }, modifier = Modifier.fillMaxWidth(),
                        singleLine = !label.contains("note", ignoreCase = true),
                        keyboardOptions = if (label.contains("Amount", ignoreCase = true))
                            KeyboardOptions(keyboardType = KeyboardType.Decimal) else KeyboardOptions.Default
                    )
                }
            }
        },
        confirmButton = { Button(onClick = { onSave(values.toList()) }) { Text("Save") } },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}

@Composable
private fun MetricCard(label: String, value: String, modifier: Modifier = Modifier) {
    Card(modifier = modifier, shape = RoundedCornerShape(18.dp)) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(value, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
            Text(label, style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun DashboardAction(title: String, subtitle: String, modifier: Modifier = Modifier, onClick: () -> Unit) {
    Card(
        onClick = onClick, modifier = modifier, shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(5.dp)) {
            Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            Text(subtitle, style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun RecordCard(title: String, subtitle: String, trailing: String) {
    Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(18.dp)) {
        Row(Modifier.padding(16.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(title, fontWeight = FontWeight.SemiBold)
                Text(subtitle, style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Text(trailing, style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.SemiBold)
        }
    }
}

@Composable
private fun EmptyState(title: String, message: String) {
    Column(Modifier.fillMaxWidth().padding(vertical = 48.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(title, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.SemiBold)
        Text(message, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

private fun formatMinor(amount: Long): String =
    BigDecimal.valueOf(amount, 2).setScale(2, RoundingMode.HALF_UP).toPlainString()

private fun formatDateTime(timestamp: Long): String =
    java.time.Instant.ofEpochMilli(timestamp).atZone(ZoneId.systemDefault())
        .format(DateTimeFormatter.ofPattern("dd MMM, hh:mm a"))
