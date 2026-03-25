package com.student.studentproductivitybot.data

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first

object FirebaseManager {
    private lateinit var auth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore

    private val _isSignedIn = MutableStateFlow(false)
    val isSignedIn: StateFlow<Boolean> = _isSignedIn

    private val _currentUserId = MutableStateFlow("")
    val currentUserId: StateFlow<String> = _currentUserId

    fun init() {
        auth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()
        _isSignedIn.value = auth.currentUser != null
        _currentUserId.value = auth.currentUser?.uid ?: ""
        android.util.Log.d("FirebaseManager", "Init - isSignedIn: ${_isSignedIn.value}")
    }

    suspend fun signIn(email: String, password: String): Boolean {
        return try {
            val result = auth.signInWithEmailAndPassword(email, password).await()
            _isSignedIn.value = true
            _currentUserId.value = result.user?.uid ?: ""
            loadFromCloud()
            true
        } catch (e: Exception) {
            false
        }
    }

    suspend fun signUp(email: String, password: String): Boolean {
        return try {
            val result = auth.createUserWithEmailAndPassword(email, password).await()
            _isSignedIn.value = true
            _currentUserId.value = result.user?.uid ?: ""
            saveToCloud()
            true
        } catch (e: Exception) {
            false
        }
    }

    // ✅ LOGOUT FUNCTION
    fun signOut() {
        auth.signOut()
        _isSignedIn.value = false
        _currentUserId.value = ""
        android.util.Log.d("FirebaseManager", "User signed out")
    }

    suspend fun saveToCloud() {
        val userId = _currentUserId.value
        if (userId.isEmpty()) return

        // Save tasks
        val tasks = SimpleStorage.getTasksFlow().first()
        firestore.collection("users").document(userId)
            .collection("tasks").document("data")
            .set(mapOf("tasks" to tasks.map { it.copy() }))
            .await()

        // Save notes
        val notes = SimpleStorage.getNotesFlow().first()
        firestore.collection("users").document(userId)
            .collection("notes").document("data")
            .set(mapOf("notes" to notes.map { it.copy() }))
            .await()

        // Save exams
        val exams = SimpleStorage.getExamsFlow().first()
        firestore.collection("users").document(userId)
            .collection("exams").document("data")
            .set(mapOf("exams" to exams.map { it.copy() }))
            .await()
    }

    suspend fun loadFromCloud() {
        val userId = _currentUserId.value
        if (userId.isEmpty()) return

        // Load tasks
        val tasksDoc = firestore.collection("users").document(userId)
            .collection("tasks").document("data").get().await()
        val tasks = (tasksDoc.get("tasks") as? List<*>)?.mapNotNull {
            (it as? Map<*, *>)?.let { map ->
                SimpleTask(
                    id = (map["id"] as? Number)?.toInt() ?: 0,
                    title = map["title"] as? String ?: "",
                    isCompleted = map["isCompleted"] as? Boolean ?: false,
                    priority = map["priority"] as? String ?: "MEDIUM",
                    createdAt = (map["createdAt"] as? Number)?.toLong() ?: 0
                )
            }
        } ?: emptyList()
        SimpleStorage.saveTasks(tasks)

        // Load notes
        val notesDoc = firestore.collection("users").document(userId)
            .collection("notes").document("data").get().await()
        val notes = (notesDoc.get("notes") as? List<*>)?.mapNotNull {
            (it as? Map<*, *>)?.let { map ->
                SimpleNote(
                    id = (map["id"] as? Number)?.toInt() ?: 0,
                    title = map["title"] as? String ?: "",
                    content = map["content"] as? String ?: "",
                    createdAt = (map["createdAt"] as? Number)?.toLong() ?: 0
                )
            }
        } ?: emptyList()
        SimpleStorage.saveNotes(notes)

        // Load exams
        val examsDoc = firestore.collection("users").document(userId)
            .collection("exams").document("data").get().await()
        val exams = (examsDoc.get("exams") as? List<*>)?.mapNotNull {
            (it as? Map<*, *>)?.let { map ->
                SimpleExam(
                    id = (map["id"] as? Number)?.toInt() ?: 0,
                    subject = map["subject"] as? String ?: "",
                    date = (map["date"] as? Number)?.toLong() ?: 0,
                    topics = map["topics"] as? String ?: "",
                    completed = map["completed"] as? Boolean ?: false
                )
            }
        } ?: emptyList()
        SimpleStorage.saveExams(exams)
    }
}