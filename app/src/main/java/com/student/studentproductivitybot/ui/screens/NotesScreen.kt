package com.student.studentproductivitybot.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.student.studentproductivitybot.data.SimpleNote
import com.student.studentproductivitybot.data.SimpleStorage
import kotlinx.coroutines.launch
import android.util.Log

// Tag for filtering logs in Logcat
private const val TAG = "NotesScreen"

@Composable
fun NotesScreen() {
    val scope = rememberCoroutineScope()

    var notes by remember { mutableStateOf(listOf<SimpleNote>()) }
    var showDialog by remember { mutableStateOf(false) }
    var newTitle by remember { mutableStateOf("") }
    var newContent by remember { mutableStateOf("") }

    // Load notes when screen opens
    LaunchedEffect(Unit) {
        Log.d(TAG, "📱 Loading notes from storage...")
        SimpleStorage.getNotesFlow().collect { loadedNotes ->
            Log.d(TAG, "📋 Loaded ${loadedNotes.size} notes")
            loadedNotes.forEach { note ->
                Log.d(TAG, "   - Note: id=${note.id}, title='${note.title}', content='${note.content}'")
            }
            notes = loadedNotes
        }
    }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(onClick = {
                Log.d(TAG, "➕ Floating button clicked - opening dialog")
                showDialog = true
            }) {
                Icon(Icons.Default.Add, "Add Note")
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            Text("📝 My Notes (${notes.size})", style = MaterialTheme.typography.headlineMedium)

            Spacer(modifier = Modifier.height(16.dp))

            if (notes.isEmpty()) {
                Log.d(TAG, "📭 No notes to display")
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("No notes yet. Tap + to add one!")
                }
            } else {
                Log.d(TAG, "📄 Displaying ${notes.size} notes")
                LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(notes) { note ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.secondaryContainer
                            )
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text("📌 ${note.title}", style = MaterialTheme.typography.titleMedium)
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(note.content, style = MaterialTheme.typography.bodyMedium)
                                    Text("ID: ${note.id}", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))
                                }
                                IconButton(onClick = {
                                    Log.d(TAG, "🗑️ Deleting note: id=${note.id}, title='${note.title}'")
                                    scope.launch {
                                        val newNotes = notes.filter { it.id != note.id }
                                        notes = newNotes
                                        SimpleStorage.saveNotes(newNotes)
                                        Log.d(TAG, "✅ Note deleted. ${newNotes.size} notes remaining")
                                    }
                                }) {
                                    Icon(Icons.Default.Delete, "Delete", tint = MaterialTheme.colorScheme.error)
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Add Note Dialog
    if (showDialog) {
        Log.d(TAG, "💬 Showing Add Note Dialog")
        AlertDialog(
            onDismissRequest = {
                Log.d(TAG, "❌ Dialog dismissed without saving")
                showDialog = false
                newTitle = ""
                newContent = ""
            },
            title = { Text("Add New Note") },
            text = {
                Column {
                    OutlinedTextField(
                        value = newTitle,
                        onValueChange = {
                            newTitle = it
                            Log.d(TAG, "✏️ Title changed: '$it'")
                        },
                        label = { Text("Title") },
                        placeholder = { Text("Enter title here") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = newContent,
                        onValueChange = {
                            newContent = it
                            Log.d(TAG, "📝 Content changed: '${it.take(50)}...'")
                        },
                        label = { Text("Content") },
                        placeholder = { Text("Enter note content here") },
                        minLines = 3,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        Log.d(TAG, "💾 SAVE BUTTON CLICKED")
                        Log.d(TAG, "   Title value: '${newTitle}'")
                        Log.d(TAG, "   Title isBlank: ${newTitle.isBlank()}")
                        Log.d(TAG, "   Content value: '${newContent}'")
                        Log.d(TAG, "   Content isBlank: ${newContent.isBlank()}")

                        if (newTitle.isNotBlank() || newContent.isNotBlank()) {
                            val finalTitle = if (newTitle.isNotBlank()) newTitle else "Untitled"
                            val nextId = (notes.maxOfOrNull { it.id } ?: 0) + 1

                            Log.d(TAG, "✅ Creating new note:")
                            Log.d(TAG, "   ID: $nextId")
                            Log.d(TAG, "   Title: '$finalTitle'")
                            Log.d(TAG, "   Content: '${newContent}'")

                            scope.launch {
                                val newNote = SimpleNote(
                                    id = nextId,
                                    title = finalTitle,
                                    content = newContent
                                )
                                val newNotes = notes + newNote
                                notes = newNotes
                                SimpleStorage.saveNotes(newNotes)
                                Log.d(TAG, "💾 Note saved! Total notes: ${newNotes.size}")

                                // Verify save
                                SimpleStorage.getNotesFlow().collect { verifyNotes ->
                                    Log.d(TAG, "🔍 Verification: ${verifyNotes.size} notes in storage")
                                    verifyNotes.forEach { note ->
                                        Log.d(TAG, "   - ${note.id}: ${note.title}")
                                    }
                                }
                            }
                            showDialog = false
                            newTitle = ""
                            newContent = ""
                            Log.d(TAG, "✨ Dialog closed after save")
                        } else {
                            Log.d(TAG, "⚠️ Both title and content are blank - not saving")
                        }
                    }
                ) {
                    Text("Save")
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    Log.d(TAG, "❌ Cancel button clicked")
                    showDialog = false
                    newTitle = ""
                    newContent = ""
                }) {
                    Text("Cancel")
                }
            }
        )
    }
}