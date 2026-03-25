package com.student.studentproductivitybot.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.student.studentproductivitybot.data.FirebaseManager
import com.student.studentproductivitybot.data.SimpleTask
import com.student.studentproductivitybot.data.SimpleStorage
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen() {
    var currentTime by remember { mutableStateOf(Calendar.getInstance()) }

    var tasks by remember { mutableStateOf(listOf<SimpleTask>()) }
    var completedTasks by remember { mutableStateOf(0) }
    var pendingTasks by remember { mutableStateOf(0) }

    LaunchedEffect(Unit) {
        SimpleStorage.getTasksFlow().collect { loadedTasks ->
            tasks = loadedTasks
            completedTasks = loadedTasks.count { it.isCompleted }
            pendingTasks = loadedTasks.count { !it.isCompleted }
        }
    }

    LaunchedEffect(Unit) {
        while (true) {
            kotlinx.coroutines.delay(60000)
            currentTime = Calendar.getInstance()
        }
    }

    val greeting = when (currentTime.get(Calendar.HOUR_OF_DAY)) {
        in 0..11 -> "Good Morning"
        in 12..16 -> "Good Afternoon"
        else -> "Good Evening"
    }

    val dateFormat = SimpleDateFormat("EEEE, MMMM d", Locale.getDefault())
    val timeFormat = SimpleDateFormat("h:mm a", Locale.getDefault())

    val totalTasks = tasks.size
    val taskProgress = if (totalTasks > 0) completedTasks.toFloat() / totalTasks else 0f

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Student Productivity Bot") },
                actions = {
                    IconButton(onClick = { FirebaseManager.signOut() }) {
                        Icon(Icons.Default.Logout, contentDescription = "Logout", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = Color.White
                )
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(MaterialTheme.colorScheme.background)
        ) {
            item {
                HeaderSection(greeting, dateFormat, timeFormat, currentTime)
            }

            // Stats Cards - Using Box with weight to fix the previous UI
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Box(modifier = Modifier.weight(1f)) {
                        StatCard(
                            value = "$completedTasks",
                            label = "Completed",
                            icon = Icons.Default.CheckCircle,
                            color = Color(0xFF4CAF50)
                        )
                    }
                    Box(modifier = Modifier.weight(1f)) {
                        StatCard(
                            value = "$pendingTasks",
                            label = "Pending",
                            icon = Icons.Default.Pending,
                            color = Color(0xFFFF9800)
                        )
                    }
                    Box(modifier = Modifier.weight(1f)) {
                        StatCard(
                            value = "$totalTasks",
                            label = "Total Tasks",
                            icon = Icons.Default.FormatListBulleted,
                            color = Color(0xFF2196F3)
                        )
                    }
                }
            }

            item {
                ProgressSection(taskProgress, completedTasks, totalTasks)
            }

            item {
                UpcomingTasksSection(tasks.filter { !it.isCompleted }.take(3))
            }

            item {
                QuoteSection()
            }
        }
    }
}

@Composable
fun StatCard(
    value: String,
    label: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    color: Color
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = color.copy(alpha = 0.1f)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(24.dp))
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = value, fontSize = 24.sp, fontWeight = FontWeight.Bold, color = color)
            Text(text = label, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f))
        }
    }
}

@Composable
fun ProgressSection(taskProgress: Float, completedTasks: Int, totalTasks: Int) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Today's Progress", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                Text("$completedTasks/$totalTasks", fontSize = 16.sp, fontWeight = FontWeight.Bold)
            }
            Spacer(modifier = Modifier.height(12.dp))
            LinearProgressIndicator(
                progress = { taskProgress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(12.dp)
                    .clip(RoundedCornerShape(6.dp)),
                color = Color(0xFF4CAF50)
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = when {
                    taskProgress >= 0.8f -> "🎉 Amazing progress! Keep going!"
                    taskProgress >= 0.5f -> "👍 You're halfway there!"
                    taskProgress >= 0.2f -> "💪 Good start! Keep pushing!"
                    else -> "✨ Add some tasks to get started!"
                },
                fontSize = 14.sp
            )
        }
    }
}

@Composable
fun UpcomingTasksSection(tasks: List<SimpleTask>) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("📋 Upcoming Tasks", fontSize = 18.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(12.dp))
            if (tasks.isEmpty()) {
                Text("No pending tasks. Great job! 🎉", color = Color.Gray)
            } else {
                tasks.forEach { task ->
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(RoundedCornerShape(4.dp))
                                .background(
                                    when(task.priority) {
                                        "HIGH" -> Color(0xFFF44336)
                                        "MEDIUM" -> Color(0xFFFF9800)
                                        else -> Color(0xFF4CAF50)
                                    }
                                )
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(task.title, modifier = Modifier.weight(1f))
                        Text(task.priority, fontSize = 12.sp, color = Color.Gray)
                    }
                    if (task != tasks.last()) HorizontalDivider()
                }
            }
        }
    }
}

@Composable
fun HeaderSection(
    greeting: String,
    dateFormat: SimpleDateFormat,
    timeFormat: SimpleDateFormat,
    currentTime: Calendar
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(200.dp)
            .background(
                brush = androidx.compose.ui.graphics.Brush.verticalGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.primary,
                        MaterialTheme.colorScheme.primary.copy(alpha = 0.7f)
                    )
                )
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            verticalArrangement = Arrangement.Bottom
        ) {
            Text(text = "$greeting,", fontSize = 20.sp, color = Color.White.copy(alpha = 0.9f))
            Text(text = "Student!", fontSize = 32.sp, fontWeight = FontWeight.Bold, color = Color.White)
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(text = dateFormat.format(currentTime.time), color = Color.White.copy(alpha = 0.8f))
                Text(text = timeFormat.format(currentTime.time), color = Color.White.copy(alpha = 0.8f))
            }
        }
    }
}

@Composable
fun QuoteSection() {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer
        )
    ) {
        Text(
            text = "✨ \"The secret of getting ahead is getting started.\" - Mark Twain",
            modifier = Modifier.padding(20.dp),
            style = MaterialTheme.typography.bodyLarge
        )
    }
}