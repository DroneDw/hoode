package com.example.mybalaka.screens.admin.sections

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun PropertiesSection(
    onListProperty: () -> Unit
) {
    Text("Properties Management", style = MaterialTheme.typography.headlineSmall)
    Spacer(Modifier.height(16.dp))
    Button(onClick = onListProperty, modifier = Modifier.fillMaxWidth()) {
        Text("List New Property")
    }
}