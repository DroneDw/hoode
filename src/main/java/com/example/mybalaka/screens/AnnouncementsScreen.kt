package com.example.mybalaka.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import coil.compose.AsyncImage
import com.example.mybalaka.viewmodel.AnnouncementViewModel
import com.example.mybalaka.model.Announcement
import com.example.mybalaka.utils.expiryBadge
import com.example.mybalaka.utils.shareAnnouncement
import com.google.firebase.auth.FirebaseAuth
import java.text.SimpleDateFormat
import java.util.Locale
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Environment
import android.util.Log
import androidx.core.content.FileProvider
import java.io.File
import java.io.FileOutputStream
import java.net.URL

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AnnouncementsScreen(
    navController: NavHostController,
    viewModel: AnnouncementViewModel = viewModel()
) {
    val announcements by viewModel.announcements.collectAsStateWithLifecycle()
    val currentUserId = FirebaseAuth.getInstance().currentUser?.uid ?: ""

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Announcements") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        if (announcements.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                Text("No announcements yet")
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(announcements) { announcement ->
                    AnnouncementCard(
                        announcement = announcement,
                        currentUserId = currentUserId,
                        onClick = {
                            navController.navigate("announcement_detail/${announcement.id}")
                            viewModel.markAsRead(announcement.id)
                        }
                    )
                }
            }
        }
    }
}

fun downloadFile(context: Context, fileUrl: String, fileName: String) {
    CoroutineScope(Dispatchers.IO).launch {
        try {
            val url = URL(fileUrl)
            val connection = url.openConnection()
            connection.connect()

            val inputStream = connection.getInputStream()
            val file = File(context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS), fileName)
            val outputStream = FileOutputStream(file)

            inputStream.use { input ->
                outputStream.use { output ->
                    input.copyTo(output)
                }
            }

            // Notify the system that a file has been downloaded
            val mediaScanIntent = Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE)
            mediaScanIntent.data = Uri.fromFile(file)
            context.sendBroadcast(mediaScanIntent)

            // Open the file
            val openIntent = Intent(Intent.ACTION_VIEW)
            openIntent.setDataAndType(Uri.fromFile(file), "application/octet-stream")
            openIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            context.startActivity(openIntent)
        } catch (e: Exception) {
            Log.e("Download", "Failed to download file: ${e.message}")
        }
    }
}

@Composable
fun AnnouncementCard(
    announcement: Announcement,
    currentUserId: String,
    onClick: () -> Unit
) {
    val isNewForUser = currentUserId !in announcement.readBy
    val context = LocalContext.current

    val dateFormatter = remember {
        SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
    }

    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {

            // 🔹 Image
            if (announcement.imageUrls.isNotEmpty()) {
                AsyncImage(
                    model = announcement.imageUrls.first(),
                    contentDescription = "Announcement image",
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(150.dp)
                        .padding(bottom = 8.dp),
                    contentScale = ContentScale.Crop
                )
            }

            // 🔹 Title + Badges
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = announcement.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )

                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {

                    if (isNewForUser) {
                        Badge { Text("NEW") }
                    }

                    expiryBadge(announcement.expiresAt)?.let { label ->
                        Badge(
                            containerColor = if (label == "Expired")
                                MaterialTheme.colorScheme.error
                            else
                                MaterialTheme.colorScheme.secondary
                        ) {
                            Text(label)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            // 🔹 Description
            Text(
                text = announcement.description,
                style = MaterialTheme.typography.bodyMedium,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(6.dp))

            // 🔹 Posted Date
            Text(
                text = "Posted: ${dateFormatter.format(announcement.createdAt.toDate())}",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )

            Spacer(modifier = Modifier.height(6.dp))

            // 🔹 Download Button (NEW FEATURE)
            if (announcement.showDownloadButton && !announcement.downloadableFileUrl.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(8.dp))

                Button(
                    onClick = {
                        downloadFile(context, announcement.downloadableFileUrl, "${announcement.title}.pdf")
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Download File")
                }
            }

            // 🔹 SHARE BUTTON (NEW FEATURE)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                IconButton(onClick = {
                    CoroutineScope(Dispatchers.Main).launch {
                        shareAnnouncement(context, announcement)
                    }
                }) {
                    Icon(
                        imageVector = Icons.Default.Share,
                        contentDescription = "Share Announcement"
                    )
                }
            }
        }
    }
}
