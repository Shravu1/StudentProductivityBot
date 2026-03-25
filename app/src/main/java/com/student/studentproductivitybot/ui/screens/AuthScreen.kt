package com.student.studentproductivitybot.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.student.studentproductivitybot.data.FirebaseManager
import kotlinx.coroutines.launch

@Composable
fun AuthScreen(
    onLoginSuccess: () -> Unit
) {
    var isLogin by remember { mutableStateOf(true) }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }

    val scope = rememberCoroutineScope()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "📚 Student Productivity Bot",
            style = MaterialTheme.typography.headlineMedium
        )

        Spacer(modifier = Modifier.height(32.dp))

        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Email") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Password") },
            visualTransformation = PasswordVisualTransformation(),
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        if (errorMessage.isNotEmpty()) {
            Text(
                text = errorMessage,
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.padding(8.dp)
            )
        }

        Button(
            onClick = {
                scope.launch {
                    isLoading = true
                    val success = if (isLogin) {
                        FirebaseManager.signIn(email, password)
                    } else {
                        FirebaseManager.signUp(email, password)
                    }
                    isLoading = false
                    if (success) {
                        onLoginSuccess()
                    } else {
                        errorMessage = if (isLogin) "Login failed" else "Sign up failed"
                    }
                }
            },
            modifier = Modifier.fillMaxWidth(),
            enabled = !isLoading
        ) {
            Text(if (isLoading) "Loading..." else if (isLogin) "Login" else "Sign Up")
        }

        Spacer(modifier = Modifier.height(16.dp))

        TextButton(
            onClick = { isLogin = !isLogin }
        ) {
            Text(if (isLogin) "Don't have an account? Sign Up" else "Already have an account? Login")
        }
    }
}