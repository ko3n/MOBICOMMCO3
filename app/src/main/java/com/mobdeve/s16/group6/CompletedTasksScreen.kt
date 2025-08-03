package com.mobdeve.s16.group6

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mobdeve.s16.group6.data.Person
import com.mobdeve.s16.group6.data.Task
import com.mobdeve.s16.group6.data.TaskStatus
import com.mobdeve.s16.group6.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CompletedTasksScreen(
    personName: String,
    tasks: List<Task>,
    householdMembers: List<Person>,
    onBackClicked: () -> Unit,
    onSettingsClicked: () -> Unit
) {
    val context = LocalContext.current

    // Filter only completed tasks
    val completedTasks = remember(tasks) { tasks.filter { it.status == TaskStatus.COMPLETED } }
    // Get the current month/year
    val calendar = Calendar.getInstance()
    val currentMonth = calendar.get(Calendar.MONTH)
    val currentYear = calendar.get(Calendar.YEAR)

    // Filter completed tasks for the current month
    val completedThisMonth = completedTasks.filter { task ->
        task.dueDateMillis?.let { millis ->
            val taskDate = Calendar.getInstance().apply { timeInMillis = millis }
            taskDate.get(Calendar.MONTH) == currentMonth && taskDate.get(Calendar.YEAR) == currentYear
        } ?: false
    }

    // Count how many tasks each household member completed this month
    val perPersonCompleted: Map<Int?, Int> = completedThisMonth
        .groupingBy { it.assigneeId }
        .eachCount()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = buildAnnotatedString {
                            withStyle(
                                style = androidx.compose.ui.text.SpanStyle(
                                    color = Color(0xFF4CAF50),
                                    fontWeight = FontWeight.ExtraBold
                                )
                            ) { append("$personName's ") }
                            withStyle(
                                style = androidx.compose.ui.text.SpanStyle(
                                    color = AppTextBlack,
                                    fontWeight = FontWeight.ExtraBold
                                )
                            ) { append("Completed Tasks") }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.Center,
                        letterSpacing = 2.sp
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBackClicked) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(Color.White)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Monthly Report (${SimpleDateFormat("MMMM yyyy").format(calendar.time)})",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = AppCardBlue,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
                Text(
                    text = "Tasks completed this month: ${completedThisMonth.size}",
                    fontSize = 16.sp,
                    color = AppTextBlack,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                // Per member summary
                householdMembers.forEach { member ->
                    val count = perPersonCompleted[member.id] ?: 0
                    Text(
                        text = "${member.name}: $count task${if (count == 1) "" else "s"} completed",
                        fontSize = 14.sp,
                        color = if (count > 0) AppDarkBlue else Color.Gray,
                        modifier = Modifier.padding(bottom = 4.dp)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                if (completedTasks.isEmpty()) {
                    Text(
                        text = "No completed tasks yet!",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = AppCardBlue,
                        modifier = Modifier.padding(top = 32.dp)
                    )
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        items(completedTasks) { task ->
                            CompletedTaskItem(
                                task = task,
                                householdMembers = householdMembers
                            )
                        }
                    }
                }
            }
            // Settings Button
            IconButton(
                onClick = {
                    onSettingsClicked()
                    Toast.makeText(context, "Settings Clicked!", Toast.LENGTH_SHORT).show()
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
}

@Composable
fun CompletedTaskItem(
    task: Task,
    householdMembers: List<Person>
) {
    val assigneeName = remember(task.assigneeId, householdMembers) {
        householdMembers.find { it.id == task.assigneeId }?.name ?: "Unassigned"
    }
    val dateTimeFormatter =
        remember { SimpleDateFormat("MMM dd, yyyy 'at' h:mm a", Locale.getDefault()) }
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
            Text(
                text = task.title,
                color = AppTextBlack,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )
            if (!task.description.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = task.description ?: "",
                    color = AppTextBlack.copy(alpha = 0.7f),
                    fontSize = 14.sp
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Info row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Completed",
                    color = Color(0xFF4CAF50),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )
                dueDateString?.let {
                    Text(
                        text = "Done: $it",
                        color = AppTextBlack,
                        fontSize = 14.sp
                    )
                }
                Text(
                    text = "By: $assigneeName",
                    color = AppCardBlue,
                    fontSize = 14.sp
                )
            }
        }
    }
}
