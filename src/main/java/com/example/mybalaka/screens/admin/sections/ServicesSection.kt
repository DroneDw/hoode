package com.example.mybalaka.screens.admin.sections

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.mybalaka.screens.admin.UserInputFields

@Composable
fun ServicesSection(
    name: String,
    email: String,
    password: String,
    phone: String,
    isLoading: Boolean,
    onNameChange: (String) -> Unit,
    onEmailChange: (String) -> Unit,
    onPasswordChange: (String) -> Unit,
    onPhoneChange: (String) -> Unit,
    onRegister: () -> Unit
) {
    Text("Services Management", style = MaterialTheme.typography.headlineSmall)
    Spacer(Modifier.height(8.dp))
    UserInputFields(name, email, password, phone, onNameChange, onEmailChange, onPasswordChange, onPhoneChange)
    Spacer(Modifier.height(12.dp))
    Button(onClick = onRegister, modifier = Modifier.fillMaxWidth(), enabled = !isLoading) {
        if (isLoading) CircularProgressIndicator(modifier = Modifier.size(20.dp)) else Text("Register Provider")
    }
}