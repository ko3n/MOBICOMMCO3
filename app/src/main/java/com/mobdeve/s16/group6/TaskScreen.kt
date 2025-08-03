package com.mobdeve.s16.group6

import android.app.Application
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.widget.DatePicker
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.material3.HorizontalDivider
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.mobdeve.s16.group6.data.Person
import com.mobdeve.s16.group6.data.RecurringInterval
import com.mobdeve.s16.group6.data.Task
import com.mobdeve.s16.group6.data.TaskPriority
import com.mobdeve.s16.group6.data.TaskStatus
import com.mobdeve.s16.group6.data.calculateStatus
import com.mobdeve.s16.group6.ui.theme.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaskScreen(
    personName: String,
    tasks: List<Task>,
    householdMembers: List<Person>,
    onBackClicked: () -> Unit,
    onSettingsClicked: () -> Unit,
    onAddTask: (Task) -> Unit,
    onUpdateTask: (Task) -> Unit,
    onDeleteTask: (Task) -> Unit,
    taskViewModel: TaskViewModel
) {
    val context = LocalContext.current
    var showCreateEditDialog by remember { mutableStateOf(false) }
    var taskToEdit by remember { mutableStateOf<Task?>(null) }
    var showDeleteConfirmationDialog by remember { mutableStateOf(false) }
    var taskToDelete by remember { mutableStateOf<Task?>(null) }
    val statusOptions = TaskStatus.values()
    var expanded by remember { mutableStateOf(false) }
    val selectedStatus by taskViewModel.taskStatusFilter.collectAsState()
    val filteredTasks by taskViewModel.filteredTasks.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = buildAnnotatedString {
                            withStyle(
                                style = SpanStyle(
                                    color = Color.Red,
                                    fontWeight = FontWeight.ExtraBold
                                )
                            ) {
                                append("${personName}'s")
                            }
                            withStyle(
                                style = SpanStyle(
                                    color = AppTextBlack,
                                    fontWeight = FontWeight.ExtraBold
                                )
                            ) {
                                append(" TO-DO's")
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.Center,
                        letterSpacing = 2.sp
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBackClicked) {
                        Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    taskToEdit = null
                    showCreateEditDialog = true
                },
                containerColor = AppCardBlue,
                contentColor = Color.White,
                shape = CircleShape
            ) {
                Icon(imageVector = Icons.Default.Add, contentDescription = "Add Task")
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(Color.White)
        ) {
            // Main content
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Filter dropdown
                Spacer(modifier = Modifier.height(16.dp))
                Box {
                    Button(onClick = { expanded = true }) {
                        Text(text = selectedStatus?.displayName() ?: "All Statuses")
                    }
                    DropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("All Statuses") },
                            onClick = {
                                taskViewModel.setTaskStatusFilter(null)
                                expanded = false
                            }
                        )
                        statusOptions.forEach { status ->
                            DropdownMenuItem(
                                text = { Text(status.displayName()) },
                                onClick = {
                                    taskViewModel.setTaskStatusFilter(status)
                                    expanded = false
                                }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                if (filteredTasks.isEmpty()) {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "No tasks yet!",
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            color = AppCardBlue
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Add new tasks using the button below.",
                            fontSize = 18.sp,
                            color = AppCardBlue.copy(alpha = 0.8f),
                            textAlign = TextAlign.Center
                        )
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        items(filteredTasks) { task ->
                            TaskItem(
                                task = task,
                                householdMembers = householdMembers,
                                onEditClick = { taskToEdit = it; showCreateEditDialog = true },
                                onDeleteClick = { taskToDelete = it; showDeleteConfirmationDialog = true },
//                                onCompleteClick = { t, checked ->
//                                    if (checked) onUpdateTask(t.copy(status = TaskStatus.COMPLETED))
//                                    else onUpdateTask(t.copy(status = calculateStatus(t.copy(status = TaskStatus.UPCOMING))))
//                                }
                                //using
                                onCompleteClick = { t, checked ->
                                    if (checked && t.status != TaskStatus.COMPLETED) {
                                        taskViewModel.markTaskCompleted(t)
                                    }
                                    else onUpdateTask(t.copy(status = calculateStatus(t.copy(status = TaskStatus.UPCOMING))))
                                }
                            )
                        }
                    }
                }
            }

            // Settings Button - direct child of Box, so align works!
            IconButton(
                onClick = {
                    onSettingsClicked()
                },
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(16.dp)
                    .size(48.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Settings,
                    contentDescription = "Settings",
                    tint = AppCardBlue,
                    modifier = Modifier.fillMaxSize()
                )
            }
        }
    }

    if (showCreateEditDialog) {
        CreateEditTaskDialog(
            task = taskToEdit,
            householdMembers = householdMembers,
            onDismiss = {
                showCreateEditDialog = false
                taskToEdit = null
            },
            onSave = { updatedTask ->
                if (taskToEdit == null) {
                    onAddTask(updatedTask)
                } else {
                    onUpdateTask(updatedTask)
                }
                showCreateEditDialog = false
                taskToEdit = null
            }
        )
    }

    if (showDeleteConfirmationDialog) {
        AlertDialog(
            onDismissRequest = {
                showDeleteConfirmationDialog = false
                taskToDelete = null
            },
            title = { Text("Delete Task") },
            text = { Text("Are you sure you want to delete '${taskToDelete?.title}'?") },
            confirmButton = {
                Button(onClick = {
                    taskToDelete?.let { onDeleteTask(it) }
                    showDeleteConfirmationDialog = false
                    taskToDelete = null
                }) {
                    Text("Delete")
                }
            },
            dismissButton = {
                Button(onClick = {
                    showDeleteConfirmationDialog = false
                    taskToDelete = null
                }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun TaskItem(
    task: Task,
    householdMembers: List<Person>,
    onEditClick: (Task) -> Unit,
    onDeleteClick: (Task) -> Unit,
    onCompleteClick: (Task, Boolean) -> Unit
) {
    val assigneeName = remember(task.assigneeId, householdMembers) {
        householdMembers.find { it.id == task.assigneeId }?.name ?: "Unassigned"
    }

    val dateTimeFormatter = remember { SimpleDateFormat("MMM dd, yyyy 'at' h:mm a", Locale.getDefault()) }
    val dueDateString = task.dueDateMillis?.let { millis ->
        dateTimeFormatter.format(Date(millis))
    }

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = AppLightBlue),
        modifier = Modifier
            .fillMaxWidth(0.9f)
            .padding(vertical = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Checkbox(
                    checked = task.status == TaskStatus.COMPLETED,
                    onCheckedChange = { checked ->
                        onCompleteClick(task, checked)
                    }
                )

                Text(
                    text = task.title,
                    color = AppTextBlack,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f)
                )

                IconButton(onClick = { onEditClick(task) }, modifier = Modifier.size(24.dp)) {
                    Icon(Icons.Default.Edit, "Edit Task", tint = AppTextBlack)
                }
                Spacer(Modifier.width(8.dp))
                IconButton(onClick = { onDeleteClick(task) }, modifier = Modifier.size(24.dp)) {
                    Icon(Icons.Default.Delete, "Delete Task", tint = Color.Red)
                }
            }

            task.description?.takeIf { it.isNotBlank() }?.let {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = it,
                    color = AppTextBlack.copy(alpha = 0.7f),
                    fontSize = 14.sp
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Chip(
                    text = when (task.status) {
                        TaskStatus.DUE_TODAY -> "Due Today"
                        TaskStatus.OVERDUE -> "Overdue"
                        TaskStatus.COMPLETED -> "Completed"
                        TaskStatus.UPCOMING -> "UPCOMING"
                    },
                    color = when (task.status) {
                        TaskStatus.COMPLETED -> Color(0xFF4CAF50)
                        TaskStatus.OVERDUE -> Color(0xFFF44336)
                        TaskStatus.DUE_TODAY -> Color(0xFFFFC107)
                        else -> AppCardBlue
                    }
                )

                Chip(text = "Priority: ${task.priority.name}", color = AppCardBlue)

                dueDateString?.let {
                    Chip(text = "Due: $it", color = AppCardBlue)
                }

                Chip(text = "Assigned to: $assigneeName", color = AppCardBlue)

                if (task.isRecurring) {
                    Chip(text = "Repeats: ${task.recurringInterval?.name ?: "N/A"}", color = AppCardBlue)
                }
            }
        }
    }
}

@Composable
fun Chip(text: String, color: Color) {
    Surface(
        shape = RoundedCornerShape(8.dp),
        color = color.copy(alpha = 0.8f),
        modifier = Modifier.wrapContentWidth()
    ) {
        Text(
            text = text,
            color = Color.White,
            fontSize = 12.sp,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateEditTaskDialog(
    task: Task?,
    householdMembers: List<Person>,
    onDismiss: () -> Unit,
    onSave: (Task) -> Unit
) {
    val isEditMode = task != null
    var title by remember { mutableStateOf(task?.title ?: "") }
    var description by remember { mutableStateOf(task?.description ?: "") }
    var selectedDueDateMillis by remember { mutableStateOf(task?.dueDateMillis) }
    var selectedHour by remember {mutableStateOf(0)}
    var selectedMinute by remember {mutableStateOf(0)}
    var selectedPriority by remember { mutableStateOf(task?.priority ?: TaskPriority.LOW) }
    var selectedAssigneeId by remember { mutableStateOf(task?.assigneeId) }
    var isRecurring by remember { mutableStateOf(task?.isRecurring ?: false) }
    var selectedRecurringInterval by remember { mutableStateOf(task?.recurringInterval) }

    val context = LocalContext.current
    val calendar = Calendar.getInstance()

    val year = calendar.get(Calendar.YEAR)
    val month = calendar.get(Calendar.MONTH)
    val day = calendar.get(Calendar.DAY_OF_MONTH)

    LaunchedEffect(Unit) {
        task?.dueDateMillis?.let {
            val cal = Calendar.getInstance().apply { timeInMillis = it }
            selectedHour = cal.get(Calendar.HOUR_OF_DAY)
            selectedMinute = cal.get(Calendar.MINUTE)
        }
    }

    val datePickerDialog = remember {
        DatePickerDialog(
            context,
            { _: DatePicker, selectedYear: Int, selectedMonth: Int, selectedDayOfMonth: Int ->
                val selectedCalendar = Calendar.getInstance().apply {
                    set(selectedYear, selectedMonth, selectedDayOfMonth)
                    set(Calendar.HOUR_OF_DAY, 0)
                    set(Calendar.MINUTE, 0)
                    set(Calendar.SECOND, 0)
                    set(Calendar.MILLISECOND, 0)
                }
                selectedDueDateMillis = selectedCalendar.timeInMillis
            }, year, month, day
        )
    }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = AppCardBlue),
            modifier = Modifier.padding(16.dp)
        ) {
            Column(
                modifier = Modifier
                    .padding(24.dp)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (isEditMode) "Edit Task" else "Create New Task",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    )
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close",
                        tint = Color.White,
                        modifier = Modifier
                            .size(24.dp)
                            .clickable { onDismiss() }
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                TextField(
                    value = title,
                    onValueChange = { title = it },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Title*", color = Color.Gray) },
                    shape = RoundedCornerShape(16.dp),
                    singleLine = true,
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = AppGray, unfocusedContainerColor = AppGray,
                        focusedIndicatorColor = Color.Transparent, unfocusedIndicatorColor = Color.Transparent,
                        cursorColor = Color.Black
                    )
                )
                Spacer(modifier = Modifier.height(12.dp))

                TextField(
                    value = description,
                    onValueChange = { description = it },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Description (Optional)", color = Color.Gray) },
                    shape = RoundedCornerShape(16.dp),
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = AppGray, unfocusedContainerColor = AppGray,
                        focusedIndicatorColor = Color.Transparent, unfocusedIndicatorColor = Color.Transparent,
                        cursorColor = Color.Black
                    )
                )
                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    val dueDateText = selectedDueDateMillis?.let {
                        SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()).format(Date(it))
                    } ?: "No Due Date"
                    Text(
                        text = "Due Date: $dueDateText",
                        color = Color.White,
                        fontSize = 16.sp,
                        modifier = Modifier.weight(1f)
                    )
                    Button(
                        onClick = { datePickerDialog.show() },
                        colors = ButtonDefaults.buttonColors(containerColor = Color.White, contentColor = AppDarkBlue)
                    ) {
                        Text("Select Date")
                    }
                    if (selectedDueDateMillis != null) {
                        Spacer(Modifier.width(8.dp))
                        IconButton(onClick = { selectedDueDateMillis = null }) {
                            Icon(Icons.Default.Close, "Clear Date", tint = Color.Red)
                        }
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))

                val timeText = String.format("%02d:%02d", selectedHour, selectedMinute)

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Due Time: $timeText",
                        color = Color.White,
                        fontSize = 16.sp,
                        modifier = Modifier.weight(1f)
                    )

                    Button(
                        onClick = {
                            val timePickerDialog = TimePickerDialog(
                                context,
                                { _, hourOfDay, minute ->
                                    selectedHour = hourOfDay
                                    selectedMinute = minute
                                },
                                selectedHour,
                                selectedMinute,
                                true // is24HourView
                            )
                            timePickerDialog.show()
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color.White,
                            contentColor = AppDarkBlue
                        )
                    ) {
                        Text("Select Time")
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))

                var expandedPriority by remember { mutableStateOf(false) }
                ExposedDropdownMenuBox(
                    expanded = expandedPriority,
                    onExpandedChange = { expandedPriority = !expandedPriority },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    TextField(
                        value = "Priority: ${selectedPriority.name}",
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Priority", color = Color.Gray) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor(),
                        shape = RoundedCornerShape(16.dp),
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = AppGray, unfocusedContainerColor = AppGray,
                            focusedIndicatorColor = Color.Transparent, unfocusedIndicatorColor = Color.Transparent,
                            cursorColor = Color.Black
                        )
                    )
                    ExposedDropdownMenu(
                        expanded = expandedPriority,
                        onDismissRequest = { expandedPriority = false }
                    ) {
                        TaskPriority.values().forEach { priority ->
                            DropdownMenuItem(
                                text = { Text(priority.name) },
                                onClick = {
                                    selectedPriority = priority
                                    expandedPriority = false
                                }
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))

                var expandedAssignee by remember { mutableStateOf(false) }
                val currentAssigneeName = householdMembers.find { it.id == selectedAssigneeId }?.name ?: "None"
                ExposedDropdownMenuBox(
                    expanded = expandedAssignee,
                    onExpandedChange = { expandedAssignee = !expandedAssignee },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    TextField(
                        value = "Assignee: $currentAssigneeName",
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Assignee", color = Color.Gray) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor(),
                        shape = RoundedCornerShape(16.dp),
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = AppGray, unfocusedContainerColor = AppGray,
                            focusedIndicatorColor = Color.Transparent, unfocusedIndicatorColor = Color.Transparent,
                            cursorColor = Color.Black
                        )
                    )
                    ExposedDropdownMenu(
                        expanded = expandedAssignee,
                        onDismissRequest = { expandedAssignee = false }
                    ) {
                        householdMembers.forEach { person ->
                            DropdownMenuItem(
                                text = { Text(person.name) },
                                onClick = {
                                    selectedAssigneeId = person.id
                                    expandedAssignee = false
                                }
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Recurring Task:", color = Color.White, fontSize = 16.sp)
                    Switch(
                        checked = isRecurring,
                        onCheckedChange = { isRecurring = it },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.White,
                            checkedTrackColor = AppDarkBlue,
                            uncheckedThumbColor = AppGray,
                            uncheckedTrackColor = Color.DarkGray
                        )
                    )
                }
                Spacer(modifier = Modifier.height(12.dp))

                if (isRecurring) {
                    var expandedInterval by remember { mutableStateOf(false) }
                    ExposedDropdownMenuBox(
                        expanded = expandedInterval,
                        onExpandedChange = { expandedInterval = !expandedInterval },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        TextField(
                            value = "Interval: ${selectedRecurringInterval?.name ?: "Select"}",
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Recurring Interval", color = Color.Gray) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .menuAnchor(),
                            shape = RoundedCornerShape(16.dp),
                            colors = TextFieldDefaults.colors(
                                focusedContainerColor = AppGray, unfocusedContainerColor = AppGray,
                                focusedIndicatorColor = Color.Transparent, unfocusedIndicatorColor = Color.Transparent,
                                cursorColor = Color.Black
                            )
                        )
                        ExposedDropdownMenu(
                            expanded = expandedInterval,
                            onDismissRequest = { expandedInterval = false }
                        ) {
                            RecurringInterval.values().forEach { interval ->
                                DropdownMenuItem(
                                    text = { Text(interval.name) },
                                    onClick = {
                                        selectedRecurringInterval = interval
                                        expandedInterval = false
                                    }
                                )
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                }

                Button(
                    onClick = {
                        if (title.isBlank()) {
                            Toast.makeText(context, "Title cannot be empty!", Toast.LENGTH_SHORT).show()
                            return@Button
                        }
                        if (isRecurring && selectedRecurringInterval == null) {
                            Toast.makeText(context, "Please select a recurring interval!", Toast.LENGTH_SHORT).show()
                            return@Button
                        }

                        val finalDueDateMillis = selectedDueDateMillis?.let {
                            Calendar.getInstance().apply {
                                timeInMillis = it
                                set(Calendar.HOUR_OF_DAY, selectedHour)
                                set(Calendar.MINUTE, selectedMinute)
                                set(Calendar.SECOND, 0)
                                set(Calendar.MILLISECOND, 0)
                            }.timeInMillis
                        }

                        val finalTask = task?.copy(
                            title = title,
                            description = description.takeIf { it.isNotBlank() },
                            dueDateMillis = finalDueDateMillis,
                            priority = selectedPriority,
                            assigneeId = selectedAssigneeId,
                            isRecurring = isRecurring,
                            recurringInterval = if (isRecurring) selectedRecurringInterval else null
                        ) ?: Task(
                            title = title,
                            description = description.takeIf { it.isNotBlank() },
                            dueDateMillis = finalDueDateMillis,
                            priority = selectedPriority,
                            assigneeId = selectedAssigneeId,
                            isRecurring = isRecurring,
                            recurringInterval = if (isRecurring) selectedRecurringInterval else null,
                            householdId = task?.householdId ?: 0
                        )
                        onSave(finalTask)
                    },
                    enabled = title.isNotBlank() && (!isRecurring || selectedRecurringInterval != null),
                    shape = RoundedCornerShape(50),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.White,
                        contentColor = AppDarkBlue
                    ),
                    modifier = Modifier.align(Alignment.End)
                ) {
                    Text(if (isEditMode) "Save Changes" else "Create Task", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

fun TaskStatus.displayName(): String = when (this) {
    TaskStatus.DUE_TODAY -> "DUE TODAY"
    TaskStatus.OVERDUE -> "OVERDUE"
    TaskStatus.COMPLETED -> "COMPLETED"
    TaskStatus.UPCOMING -> "UPCOMING"
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun TaskScreenPreview() {
    val previewTaskViewModel = remember { PreviewTaskViewModel() }
    ChoreoUITheme {
        TaskScreen(
            personName = "John Doe",
            tasks = emptyList(),
            householdMembers = emptyList(),
            onBackClicked = {},
            onSettingsClicked = {},
            onAddTask = {},
            onUpdateTask = {},
            onDeleteTask = {},
            taskViewModel = previewTaskViewModel
        )
    }
}

// Helper for preview only
class PreviewTaskViewModel : TaskViewModel(Application()){
    private val _taskStatusFilter = MutableStateFlow<TaskStatus?>(null)
    override val taskStatusFilter: StateFlow<TaskStatus?> = _taskStatusFilter

    private val _filteredTasks = MutableStateFlow<List<Task>>(emptyList())
    override val filteredTasks: StateFlow<List<Task>> = _filteredTasks

    override fun setTaskStatusFilter(status: TaskStatus?) {
        _taskStatusFilter.value = status
        _filteredTasks.value = emptyList()
    }
}