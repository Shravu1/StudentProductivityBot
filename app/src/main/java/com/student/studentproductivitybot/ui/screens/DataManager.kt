package com.student.studentproductivitybot.screens

import android.content.Context
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

// Create DataStore
private val Context.dataStore by preferencesDataStore(name = "student_bot_data")

object DataManager {
    private lateinit var appContext: Context

    // Task stats
    private val _completedTasks = MutableStateFlow(0)
    val completedTasks: StateFlow<Int> = _completedTasks

    private val _totalTasks = MutableStateFlow(0)
    val totalTasks: StateFlow<Int> = _totalTasks

    // Pomodoro stats
    private val _focusHours = MutableStateFlow(0.0)
    val focusHours: StateFlow<Double> = _focusHours

    private val _pomodoroSessions = MutableStateFlow(0)
    val pomodoroSessions: StateFlow<Int> = _pomodoroSessions

    // Recent activities
    private val _recentActivities = MutableStateFlow<List<Activity>>(emptyList())
    val recentActivities: StateFlow<List<Activity>> = _recentActivities

    // Loading state
    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading

    fun init(context: Context) {
        appContext = context.applicationContext
        // Load data in background
        kotlinx.coroutines.GlobalScope.launch {
            loadAllData()
        }
    }

    private suspend fun loadAllData() {
        try {
            // Load completed tasks
            val completed = appContext.dataStore.data.first()[intPreferencesKey("completed_tasks")] ?: 0
            _completedTasks.value = completed

            // Load total tasks
            val total = appContext.dataStore.data.first()[intPreferencesKey("total_tasks")] ?: 0
            _totalTasks.value = total

            // Load focus hours
            val hours = appContext.dataStore.data.first()[doublePreferencesKey("focus_hours")] ?: 0.0
            _focusHours.value = hours

            // Load pomodoro sessions
            val sessions = appContext.dataStore.data.first()[intPreferencesKey("pomodoro_sessions")] ?: 0
            _pomodoroSessions.value = sessions

            // Load recent activities (store as string list)
            val activitiesJson = appContext.dataStore.data.first()[stringPreferencesKey("activities")] ?: ""
            if (activitiesJson.isNotEmpty()) {
                // Simple parsing without serialization
                val activities = mutableListOf<Activity>()
                activitiesJson.split("|||").forEach { activityStr ->
                    val parts = activityStr.split("|")
                    if (parts.size >= 3) {
                        activities.add(Activity(
                            description = parts[0],
                            timestamp = parts[1].toLong(),
                            type = ActivityType.valueOf(parts[2])
                        ))
                    }
                }
                _recentActivities.value = activities
            }
        } catch (e: Exception) {
            // Error loading data, keep defaults
        } finally {
            _isLoading.value = false
        }
    }

    private fun saveAllData() {
        kotlinx.coroutines.GlobalScope.launch {
            try {
                appContext.dataStore.edit { preferences ->
                    preferences[intPreferencesKey("completed_tasks")] = _completedTasks.value
                    preferences[intPreferencesKey("total_tasks")] = _totalTasks.value
                    preferences[doublePreferencesKey("focus_hours")] = _focusHours.value
                    preferences[intPreferencesKey("pomodoro_sessions")] = _pomodoroSessions.value

                    // Save activities as simple string
                    val activitiesStr = _recentActivities.value.joinToString("|||") { activity ->
                        "${activity.description}|${activity.timestamp}|${activity.type.name}"
                    }
                    preferences[stringPreferencesKey("activities")] = activitiesStr
                }
            } catch (e: Exception) {
                // Error saving data
            }
        }
    }

    fun addCompletedTask() {
        _completedTasks.value += 1
        _totalTasks.value += 1
        addActivity("Completed a task", ActivityType.TASK)
        saveAllData()
    }

    fun addPomodoroSession(minutes: Int) {
        _pomodoroSessions.value += 1
        _focusHours.value += minutes / 60.0
        addActivity("Completed Pomodoro session", ActivityType.POMODORO)
        saveAllData()
    }

    fun addNote() {
        addActivity("Added a new note", ActivityType.NOTE)
        saveAllData()
    }

    private fun addActivity(description: String, type: ActivityType) {
        val newActivity = Activity(
            description = description,
            timestamp = System.currentTimeMillis(),
            type = type
        )
        _recentActivities.value = listOf(newActivity) + _recentActivities.value
    }
}

data class Activity(
    val description: String,
    val timestamp: Long,
    val type: ActivityType
)

enum class ActivityType {
    TASK, POMODORO, NOTE
}