package com.student.studentproductivitybot

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.student.studentproductivitybot.data.FirebaseManager
import com.student.studentproductivitybot.data.SimpleStorage
import com.student.studentproductivitybot.ui.screens.PomodoroScreen
import com.student.studentproductivitybot.ui.screens.*
import com.student.studentproductivitybot.ui.theme.StudentProductivityBotTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        SimpleStorage.init(applicationContext)
        FirebaseManager.init()  // ✅ Initialize Firebase

        setContent {
            StudentProductivityBotTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val isSignedIn by FirebaseManager.isSignedIn.collectAsState()

                    if (isSignedIn) {
                        ProductivityApp()
                    } else {
                        AuthScreen(
                            onLoginSuccess = { /* User logged in */ }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ProductivityApp() {
    val navController = rememberNavController()

    val items = listOf(
        Screen.Dashboard,
        Screen.Tasks,
        Screen.Pomodoro,
        Screen.Notes,
        Screen.ExamPrep
    )

    Scaffold(
        bottomBar = {
            NavigationBar {
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentRoute = navBackStackEntry?.destination?.route

                items.forEach { screen ->
                    NavigationBarItem(
                        icon = {
                            when (screen) {
                                Screen.Dashboard -> Icon(Icons.Default.Dashboard, contentDescription = screen.title)
                                Screen.Tasks -> Icon(Icons.Default.Checklist, contentDescription = screen.title)
                                Screen.Pomodoro -> Icon(Icons.Default.Timer, contentDescription = screen.title)
                                Screen.Notes -> Icon(Icons.Default.Note, contentDescription = screen.title)
                                Screen.ExamPrep -> Icon(Icons.Default.School, contentDescription = screen.title)
                            }
                        },
                        label = { Text(screen.title) },
                        selected = currentRoute == screen.route,
                        onClick = {
                            navController.navigate(screen.route) {
                                popUpTo(navController.graph.startDestinationId) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    )
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Dashboard.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(Screen.Dashboard.route) {
                DashboardScreen()
            }
            composable(Screen.Tasks.route) {
                TasksScreen()
            }
            composable(Screen.Pomodoro.route) {
                PomodoroScreen()
            }
            composable(Screen.Notes.route) {
                NotesScreen()
            }
            composable(Screen.ExamPrep.route) {
                ExamPrepScreen()
            }
        }
    }
}

sealed class Screen(val route: String, val title: String) {
    object Dashboard : Screen("dashboard", "Dashboard")
    object Tasks : Screen("tasks", "Tasks")
    object Pomodoro : Screen("pomodoro", "Timer")
    object Notes : Screen("notes", "Notes")
    object ExamPrep : Screen("examprep", "Exams")
}