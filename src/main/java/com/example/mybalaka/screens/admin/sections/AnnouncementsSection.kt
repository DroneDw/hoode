package com.example.mybalaka.screens.admin.sections

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun AnnouncementsSection(
    onAddAnnouncement: () -> Unit,
    onViewAnnouncements: () -> Unit
) {
    Text("Announcements Management", style = MaterialTheme.typography.headlineSmall)
    Spacer(Modifier.height(16.dp))
    Button(onClick = onAddAnnouncement, modifier = Modifier.fillMaxWidth()) {
        Text("Add New Announcement")
    }
    Spacer(Modifier.height(8.dp))
    Button(onClick = onViewAnnouncements, modifier = Modifier.fillMaxWidth()) {
        Text("View All Announcements")
    }
}