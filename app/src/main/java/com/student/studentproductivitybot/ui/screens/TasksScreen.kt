package com.student.studentproductivitybot.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.student.studentproductivitybot.data.SimpleTask
import com.student.studentproductivitybot.data.SimpleStorage
import kotlinx.coroutines.launch

@Composable
fun TasksScreen() {
    val scope = rememberCoroutineScope()

    var tasks by remember { mutableStateOf(listOf<SimpleTask>()) }
    var showDialog by remember { mutableStateOf(false) }
    var newTaskTitle by remember { mutableStateOf("") }
    var selectedPriority by remember { mutableStateOf("MEDIUM") }

    // Load tasks when screen opens
    LaunchedEffect(Unit) {
        SimpleStorage.getTasksFlow().collect { loadedTasks ->
            tasks = loadedTasks
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "✅ My Tasks (${tasks.size})",
                style = MaterialTheme.typography.headlineMedium
            )

            FloatingActionButton(
                onClick = { showDialog = true },
                modifier = Modifier.size(48.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Task")
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Tasks List
        if (tasks.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text("No tasks yet. Tap + to add one!")
            }
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(tasks) { task ->
                    TaskCard(
                        task = task,
                        onToggleComplete = {
                            scope.launch {
                                val updatedTasks = tasks.map {
                                    if (it.id == task.id) {
                                        it.copy(isCompleted = !it.isCompleted)
                                    } else it
                                }
                                tasks = updatedTasks
                                SimpleStorage.saveTasks(updatedTasks)
                            }
                        },
                        onDelete = {
                            scope.launch {
                                val updatedTasks = tasks.filter { it.id != task.id }
                                tasks = updatedTasks
                                SimpleStorage.saveTasks(updatedTasks)
                            }
                        }
                    )
                }
            }
        }
    }

    // Add Task Dialog
    if (showDialog) {
        AlertDialog(
            onDismissRequest = {
                showDialog = false
                newTaskTitle = ""
            },
            title = { Text("Add New Task") },
            text = {
                Column {
                    OutlinedTextField(
                        value = newTaskTitle,
                        onValueChange = { newTaskTitle = it },
                        label = { Text("Task Title") },
                        placeholder = { Text("Enter task name here") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text("Priority", style = MaterialTheme.typography.labelLarge)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf("HIGH", "MEDIUM", "LOW").forEach { priority ->
                            FilterChip(
                                selected = selectedPriority == priority,
                                onClick = { selectedPriority = priority },
                                label = { Text(priority) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = when(priority) {
                                        "HIGH" -> Color(0xFFF44336).copy(alpha = 0.2f)
                                        "MEDIUM" -> Color(0xFFFF9800).copy(alpha = 0.2f)
                                        else -> Color(0xFF4CAF50).copy(alpha = 0.2f)
                                    }
                                )
                            )
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        // ✅ Make sure title is not blank
                        val trimmedTitle = newTaskTitle.trim()
                        if (trimmedTitle.isNotEmpty()) {
                            scope.launch {
                                val newId = (tasks.maxOfOrNull { it.id } ?: 0) + 1
                                val newTask = SimpleTask(
                                    id = newId,
                                    title = trimmedTitle,  // ✅ Use trimmed title
                                    priority = selectedPriority
                                )
                                val updatedTasks = tasks + newTask
                                tasks = updatedTasks
                                SimpleStorage.saveTasks(updatedTasks)
                            }
                            showDialog = false
                            newTaskTitle = ""
                        }
                    }
                ) {
                    Text("Add")
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    showDialog = false
                    newTaskTitle = ""
                }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
fun TaskCard(
    task: SimpleTask,
    onToggleComplete: () -> Unit,
    onDelete: () -> Unit
) {
    val priorityColor = when(task.priority) {
        "HIGH" -> Color(0xFFF44336)
        "MEDIUM" -> Color(0xFFFF9800)
        else -> Color(0xFF4CAF50)
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (task.isCompleted)
                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
            else
                priorityColor.copy(alpha = 0.1f)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onToggleComplete) {
                Icon(
                    if (task.isCompleted) Icons.Default.CheckCircle else Icons.Default.RadioButtonUnchecked,
                    contentDescription = if (task.isCompleted) "Completed" else "Mark Complete",
                    tint = priorityColor
                )
            }

            // ✅ Task title with priority badge
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = task.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = if (task.isCompleted) FontWeight.Normal else FontWeight.Medium,
                    color = if (task.isCompleted)
                        MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                    else
                        MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = task.priority,
                    fontSize = 10.sp,
                    color = priorityColor,
                    modifier = Modifier.padding(top = 2.dp)
                )
            }

            IconButton(onClick = onDelete) {
                Icon(
                    Icons.Default.Delete,
                    contentDescription = "Delete",
                    tint = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}