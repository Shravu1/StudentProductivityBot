package com.student.studentproductivitybot.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import androidx.lifecycle.viewmodel.compose.viewModel
import com.student.studentproductivitybot.screens.StudyViewModel

@Composable
fun PomodoroScreen(
    // ✅ Pass ViewModel as parameter
    viewModel: StudyViewModel = viewModel()
) {
    var timerValue by remember { mutableStateOf(25 * 60) }
    var isRunning by remember { mutableStateOf(false) }
    var isBreak by remember { mutableStateOf(false) }
    var sessionCount by remember { mutableStateOf(0) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Timer Circle
        Box(
            modifier = Modifier
                .size(280.dp)
                .clip(CircleShape)
                .background(
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                ),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = String.format("%02d:%02d", timerValue / 60, timerValue % 60),
                    fontSize = 48.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isBreak) Color(0xFF4CAF50) else MaterialTheme.colorScheme.primary
                )
                Text(
                    text = if (isBreak) "Break Time" else "Focus Time",
                    fontSize = 16.sp,
                    color = Color.Gray
                )
            }
        }

        Spacer(modifier = Modifier.height(48.dp))

        // Control Buttons
        Row(
            horizontalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            Button(
                onClick = { isRunning = !isRunning },
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isRunning) Color(0xFFF44336) else Color(0xFF4CAF50)
                ),
                modifier = Modifier.size(70.dp)
            ) {
                Icon(
                    if (isRunning) Icons.Default.Pause else Icons.Default.PlayArrow,
                    contentDescription = if (isRunning) "Pause" else "Start"
                )
            }

            Button(
                onClick = {
                    isRunning = false
                    timerValue = if (isBreak) 5 * 60 else 25 * 60
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.secondary
                ),
                modifier = Modifier.size(70.dp)
            ) {
                Icon(
                    Icons.Default.RestartAlt,
                    contentDescription = "Reset"
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Session Counter
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer
            )
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("$sessionCount", fontSize = 20.sp, fontWeight = FontWeight.Bold)
                    Text("Sessions", color = Color.Gray)
                }
                VerticalDivider(
                    modifier = Modifier.height(40.dp),
                    thickness = 1.dp,
                    color = Color.Gray.copy(alpha = 0.3f)
                )
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("25", fontSize = 20.sp, fontWeight = FontWeight.Bold)
                    Text("Focus", color = Color.Gray)
                }
                VerticalDivider(
                    modifier = Modifier.height(40.dp),
                    thickness = 1.dp,
                    color = Color.Gray.copy(alpha = 0.3f)
                )
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("5", fontSize = 20.sp, fontWeight = FontWeight.Bold)
                    Text("Breaks", color = Color.Gray)
                }
            }
        }
    }

    // Timer logic
    LaunchedEffect(isRunning) {
        while (isRunning && timerValue > 0) {
            delay(1000L)
            timerValue--
        }

        // When timer completes - Use the viewModel parameter
        if (timerValue == 0) {
            if (!isBreak) {
                sessionCount++
                viewModel.pomodoroCompleted(25)  // ✅ Using viewModel parameter
            }
            isRunning = false
            isBreak = !isBreak
            timerValue = if (isBreak) 5 * 60 else 25 * 60
        }
    }
}