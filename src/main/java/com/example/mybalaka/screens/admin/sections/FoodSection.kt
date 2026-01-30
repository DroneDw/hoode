package com.example.mybalaka.screens.admin.sections

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.mybalaka.screens.admin.UserInputFields

@Composable
fun FoodSection(
    cookName: String,
    cookEmail: String,
    cookPassword: String,
    cookPhone: String,
    isLoading: Boolean,
    onCookNameChange: (String) -> Unit,
    onCookEmailChange: (String) -> Unit,
    onCookPasswordChange: (String) -> Unit,
    onCookPhoneChange: (String) -> Unit,
    onRegisterCook: () -> Unit,
    onAddFoodItem: () -> Unit
) {
    Text("Food Management", style = MaterialTheme.typography.headlineSmall)
    Spacer(Modifier.height(8.dp))
    UserInputFields(cookName, cookEmail, cookPassword, cookPhone, onCookNameChange, onCookEmailChange, onCookPasswordChange, onCookPhoneChange)
    Spacer(Modifier.height(12.dp))
    Button(onClick = onRegisterCook, modifier = Modifier.fillMaxWidth(), enabled = !isLoading) {
        if (isLoading) CircularProgressIndicator(modifier = Modifier.size(20.dp)) else Text("Register Cook")
    }
    Spacer(Modifier.height(16.dp))
    Button(onClick = onAddFoodItem, modifier = Modifier.fillMaxWidth()) {
        Text("Add Food Item")
    }
}