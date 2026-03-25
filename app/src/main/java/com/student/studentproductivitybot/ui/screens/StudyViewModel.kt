package com.student.studentproductivitybot.screens

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.Calendar

// ActivityType is defined in DataManager.kt
import com.student.studentproductivitybot.screens.ActivityType
import com.student.studentproductivitybot.screens.Activity

data class StudyStats(
    val tasksCompleted: Int = 0,
    val focusHours: Double = 0.0,
    val studyStreak: Int = 0,
    val totalSessions: Int = 0
)

data class ActivityItem(
    val description: String,
    val timestamp: Long,
    val type: ActivityType,
    val id: Int = (0..10000).random()
)

class StudyViewModel : ViewModel() {
    // Stats
    private val _stats = MutableStateFlow(StudyStats())
    val stats: StateFlow<StudyStats> = _stats.asStateFlow()

    // Activities
    private val _activities = MutableStateFlow<List<ActivityItem>>(emptyList())
    val activities: StateFlow<List<ActivityItem>> = _activities.asStateFlow()

    // Streak tracking
    private var lastStudyDate: Calendar? = null

    init {
        // Load saved data
        loadSampleData()
        checkStreak()
    }

    fun taskCompleted() {
        viewModelScope.launch {
            _stats.value = _stats.value.copy(
                tasksCompleted = _stats.value.tasksCompleted + 1
            )
            addActivity("Completed a task", ActivityType.TASK)
            checkStreak()
        }
    }

    fun pomodoroCompleted(minutes: Int) {
        viewModelScope.launch {
            _stats.value = _stats.value.copy(
                focusHours = _stats.value.focusHours + (minutes / 60.0),
                totalSessions = _stats.value.totalSessions + 1
            )
            addActivity("Completed $minutes min focus session", ActivityType.POMODORO)
            checkStreak()
        }
    }

    fun noteAdded() {
        viewModelScope.launch {
            addActivity("Added a new note", ActivityType.NOTE)
            checkStreak()
        }
    }

    private fun addActivity(description: String, type: ActivityType) {
        val newActivity = ActivityItem(
            description = description,
            timestamp = System.currentTimeMillis(),
            type = type
        )
        _activities.value = listOf(newActivity) + _activities.value
    }

    private fun checkStreak() {
        val today = Calendar.getInstance()
        val lastDate = lastStudyDate

        if (lastDate == null) {
            _stats.value = _stats.value.copy(studyStreak = 1)
            lastStudyDate = today
        } else {
            val daysDiff = getDaysDifference(lastDate, today)
            when {
                daysDiff == 0 -> { }
                daysDiff == 1 -> {
                    _stats.value = _stats.value.copy(
                        studyStreak = _stats.value.studyStreak + 1
                    )
                    lastStudyDate = today
                }
                else -> {
                    _stats.value = _stats.value.copy(studyStreak = 1)
                    lastStudyDate = today
                }
            }
        }
    }

    private fun getDaysDifference(date1: Calendar, date2: Calendar): Int {
        val msDiff = date2.timeInMillis - date1.timeInMillis
        return (msDiff / (1000 * 60 * 60 * 24)).toInt()
    }

    private fun loadSampleData() {
        _activities.value = listOf(
            ActivityItem(
                description = "Completed Mathematics homework",
                timestamp = System.currentTimeMillis() - (30 * 60 * 1000),
                type = ActivityType.TASK
            ),
            ActivityItem(
                description = "Completed 25 min focus session",
                timestamp = System.currentTimeMillis() - (2 * 60 * 60 * 1000),
                type = ActivityType.POMODORO
            ),
            ActivityItem(
                description = "Added Physics notes",
                timestamp = System.currentTimeMillis() - (5 * 60 * 60 * 1000),
                type = ActivityType.NOTE
            )
        )

        _stats.value = StudyStats(
            tasksCompleted = 8,
            focusHours = 12.5,
            studyStreak = 5,
            totalSessions = 15
        )
    }
}