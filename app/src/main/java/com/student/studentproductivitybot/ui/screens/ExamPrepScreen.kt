package com.student.studentproductivitybot.ui.screens

import android.app.DatePickerDialog
import android.widget.DatePicker
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.student.studentproductivitybot.data.SimpleExam
import com.student.studentproductivitybot.data.SimpleStorage
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun ExamPrepScreen() {
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    var exams by remember { mutableStateOf(listOf<SimpleExam>()) }
    var showAddDialog by remember { mutableStateOf(false) }
    var selectedTab by remember { mutableStateOf(0) }

    // Load exams from storage
    LaunchedEffect(Unit) {
        SimpleStorage.getExamsFlow().collect { loadedExams ->
            exams = loadedExams
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
                text = "📚 Exam Prep (${exams.size})",
                style = MaterialTheme.typography.headlineMedium
            )

            FloatingActionButton(
                onClick = { showAddDialog = true },
                modifier = Modifier.size(48.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Exam")
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Tabs
        TabRow(selectedTabIndex = selectedTab) {
            Tab(
                selected = selectedTab == 0,
                onClick = { selectedTab = 0 },
                text = { Text("Upcoming") }
            )
            Tab(
                selected = selectedTab == 1,
                onClick = { selectedTab = 1 },
                text = { Text("Completed") }
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Exam List
        val filteredExams = if (selectedTab == 0) {
            exams.filter { !it.completed }
        } else {
            exams.filter { it.completed }
        }

        if (filteredExams.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        Icons.Default.School,
                        contentDescription = null,
                        modifier = Modifier.size(64.dp),
                        tint = Color.Gray
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = if (selectedTab == 0)
                            "No upcoming exams\nTap + to add one"
                        else
                            "No completed exams",
                        style = MaterialTheme.typography.bodyLarge,
                        color = Color.Gray
                    )
                }
            }
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(filteredExams) { exam ->
                    ExamCard(
                        exam = exam,
                        onComplete = {
                            scope.launch {
                                val updatedExams = exams.map {
                                    if (it.id == exam.id) it.copy(completed = !it.completed) else it
                                }
                                exams = updatedExams
                                SimpleStorage.saveExams(updatedExams)
                            }
                        },
                        onDelete = {
                            scope.launch {
                                val updatedExams = exams.filter { it.id != exam.id }
                                exams = updatedExams
                                SimpleStorage.saveExams(updatedExams)
                            }
                        }
                    )
                }
            }
        }
    }

    // Add Exam Dialog with Date Picker
    if (showAddDialog) {
        AddExamDialog(
            context = context,
            onDismiss = { showAddDialog = false },
            onAdd = { newExam ->
                scope.launch {
                    val updatedExams = exams + newExam
                    exams = updatedExams
                    SimpleStorage.saveExams(updatedExams)
                }
                showAddDialog = false
            }
        )
    }
}

@Composable
fun ExamCard(
    exam: SimpleExam,
    onComplete: () -> Unit,
    onDelete: () -> Unit
) {
    val daysLeft = getDaysLeft(exam.date)
    val examColor = when {
        daysLeft < 0 -> Color.Red
        daysLeft <= 3 -> Color(0xFFFF9800)
        else -> Color(0xFF4CAF50)
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (exam.completed)
                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
            else
                examColor.copy(alpha = 0.1f)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = exam.subject,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (exam.completed) Color.Gray else examColor
                )

                Row {
                    IconButton(onClick = onComplete) {
                        Icon(
                            if (exam.completed) Icons.Default.CheckCircle else Icons.Default.RadioButtonUnchecked,
                            contentDescription = if (exam.completed) "Completed" else "Mark Complete",
                            tint = if (exam.completed) Color.Gray else examColor
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

            // Date display
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Default.Event,
                    contentDescription = null,
                    tint = examColor,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = formatDate(exam.date),
                    fontSize = 14.sp,
                    color = examColor
                )
            }

            // Days left
            if (!exam.completed) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = when {
                        daysLeft < 0 -> "Past due!"
                        daysLeft == 0 -> "Today!"
                        daysLeft == 1 -> "Tomorrow!"
                        else -> "$daysLeft days left"
                    },
                    fontSize = 12.sp,
                    color = when {
                        daysLeft < 0 -> Color.Red
                        daysLeft <= 3 -> Color(0xFFFF9800)
                        else -> examColor
                    },
                    fontWeight = if (daysLeft <= 3) FontWeight.Bold else FontWeight.Normal
                )
            }

            // Topics
            if (exam.topics.isNotBlank()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "📖 Topics: ${exam.topics}",
                    fontSize = 12.sp,
                    color = Color.Gray
                )
            }
        }
    }
}

@Composable
fun AddExamDialog(
    context: android.content.Context,
    onDismiss: () -> Unit,
    onAdd: (SimpleExam) -> Unit
) {
    var subject by remember { mutableStateOf("") }
    var topics by remember { mutableStateOf("") }

    // Date picker state - using mutableState to trigger recomposition
    val calendar = Calendar.getInstance()
    var selectedDate by remember { mutableStateOf(calendar.timeInMillis) }
    var showDatePicker by remember { mutableStateOf(false) }

    val dateFormat = SimpleDateFormat("EEEE, MMMM d, yyyy", Locale.getDefault())

    // Update the displayed date when selectedDate changes
    val displayedDate = remember(selectedDate) {
        dateFormat.format(Date(selectedDate))
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add New Exam") },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedTextField(
                    value = subject,
                    onValueChange = { subject = it },
                    label = { Text("Subject") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                // Date Picker Button
                OutlinedTextField(
                    value = displayedDate,
                    onValueChange = {},
                    label = { Text("Exam Date") },
                    readOnly = true,
                    trailingIcon = {
                        IconButton(onClick = { showDatePicker = true }) {
                            Icon(Icons.Default.DateRange, contentDescription = "Pick Date")
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = topics,
                    onValueChange = { topics = it },
                    label = { Text("Topics (comma separated)") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    if (subject.isNotBlank()) {
                        val newExam = SimpleExam(
                            id = (0..10000).random(),
                            subject = subject,
                            date = selectedDate,
                            topics = topics,
                            completed = false
                        )
                        onAdd(newExam)
                    }
                }
            ) {
                Text("Add")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )

    // Date Picker Dialog
    if (showDatePicker) {
        val currentCalendar = Calendar.getInstance().apply {
            timeInMillis = selectedDate
        }
        val year = currentCalendar.get(Calendar.YEAR)
        val month = currentCalendar.get(Calendar.MONTH)
        val day = currentCalendar.get(Calendar.DAY_OF_MONTH)

        DatePickerDialog(
            context,
            { _: DatePicker, selectedYear: Int, selectedMonth: Int, selectedDay: Int ->
                val pickedCalendar = Calendar.getInstance()
                pickedCalendar.set(selectedYear, selectedMonth, selectedDay, 0, 0, 0)
                selectedDate = pickedCalendar.timeInMillis
                showDatePicker = false
            },
            year, month, day
        ).apply {
            setOnDismissListener {
                showDatePicker = false
            }
            show()
        }
    }
}

// Helper functions
fun getDaysLeft(dateMillis: Long): Int {
    val today = Calendar.getInstance()
    today.set(Calendar.HOUR_OF_DAY, 0)
    today.set(Calendar.MINUTE, 0)
    today.set(Calendar.SECOND, 0)
    today.set(Calendar.MILLISECOND, 0)

    val examDate = Calendar.getInstance().apply {
        timeInMillis = dateMillis
        set(Calendar.HOUR_OF_DAY, 0)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
    }

    val diff = examDate.timeInMillis - today.timeInMillis
    return (diff / (1000 * 60 * 60 * 24)).toInt()
}

fun formatDate(dateMillis: Long): String {
    val date = Date(dateMillis)
    return SimpleDateFormat("MMM d, yyyy", Locale.getDefault()).format(date)
}