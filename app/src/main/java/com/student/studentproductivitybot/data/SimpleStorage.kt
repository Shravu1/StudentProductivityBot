package com.student.studentproductivitybot.data

import android.content.Context
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

// Data classes
data class SimpleTask(
    val id: Int,
    val title: String,
    val isCompleted: Boolean = false,
    val priority: String = "MEDIUM",
    val createdAt: Long = System.currentTimeMillis()
)

data class SimpleNote(
    val id: Int,
    val title: String,
    val content: String,
    val createdAt: Long = System.currentTimeMillis()
)

data class SimpleExam(
    val id: Int,
    val subject: String,
    val date: Long,
    val topics: String = "",
    val completed: Boolean = false
)

private val Context.dataStore by preferencesDataStore(name = "student_bot_data")

object SimpleStorage {
    private lateinit var appContext: Context

    private val NOTES_KEY = stringPreferencesKey("notes")
    private val TASKS_KEY = stringPreferencesKey("tasks")
    private val EXAMS_KEY = stringPreferencesKey("exams")

    fun init(ctx: Context) {
        appContext = ctx.applicationContext
    }

    // ========== NOTES ==========
    suspend fun saveNotes(notes: List<SimpleNote>) {
        val notesString = notes.joinToString("|||") { "${it.id}|${it.title}|${it.content}|${it.createdAt}" }
        appContext.dataStore.edit { preferences ->
            preferences[NOTES_KEY] = notesString
        }
    }

    fun getNotesFlow(): Flow<List<SimpleNote>> {
        return appContext.dataStore.data.map { preferences ->
            val notesString = preferences[NOTES_KEY] ?: return@map emptyList()
            notesString.split("|||").mapNotNull { noteStr ->
                val parts = noteStr.split("|")
                if (parts.size >= 4) {
                    SimpleNote(
                        id = parts[0].toIntOrNull() ?: 0,
                        title = parts[1],
                        content = parts[2],
                        createdAt = parts[3].toLongOrNull() ?: System.currentTimeMillis()
                    )
                } else null
            }
        }
    }

    // ========== TASKS ==========
    suspend fun saveTasks(tasks: List<SimpleTask>) {
        val tasksString = tasks.joinToString("|||") { "${it.id}|${it.title}|${it.isCompleted}|${it.priority}|${it.createdAt}" }
        appContext.dataStore.edit { preferences ->
            preferences[TASKS_KEY] = tasksString
        }
    }

    fun getTasksFlow(): Flow<List<SimpleTask>> {
        return appContext.dataStore.data.map { preferences ->
            val tasksString = preferences[TASKS_KEY] ?: return@map emptyList()
            tasksString.split("|||").mapNotNull { taskStr ->
                val parts = taskStr.split("|")
                if (parts.size >= 5) {
                    SimpleTask(
                        id = parts[0].toIntOrNull() ?: 0,
                        title = parts[1],
                        isCompleted = parts[2].toBooleanStrictOrNull() ?: false,
                        priority = parts[3],
                        createdAt = parts[4].toLongOrNull() ?: System.currentTimeMillis()
                    )
                } else null
            }
        }
    }

    // ========== EXAMS ==========
    suspend fun saveExams(exams: List<SimpleExam>) {
        val examsString = exams.joinToString("|||") { "${it.id}|${it.subject}|${it.date}|${it.topics}|${it.completed}" }
        appContext.dataStore.edit { preferences ->
            preferences[EXAMS_KEY] = examsString
        }
    }

    fun getExamsFlow(): Flow<List<SimpleExam>> {
        return appContext.dataStore.data.map { preferences ->
            val examsString = preferences[EXAMS_KEY] ?: return@map emptyList()
            examsString.split("|||").mapNotNull { examStr ->
                val parts = examStr.split("|")
                if (parts.size >= 5) {
                    SimpleExam(
                        id = parts[0].toIntOrNull() ?: 0,
                        subject = parts[1],
                        date = parts[2].toLongOrNull() ?: 0,
                        topics = parts[3],
                        completed = parts[4].toBooleanStrictOrNull() ?: false
                    )
                } else null
            }
        }
    }
}