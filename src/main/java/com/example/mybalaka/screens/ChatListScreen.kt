package com.example.mybalaka.screens.chat

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.clickable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.mybalaka.model.ChatRoom
import com.example.mybalaka.model.User
import com.example.mybalaka.viewmodel.ChatViewModel
import com.google.firebase.auth.FirebaseAuth
import java.text.SimpleDateFormat
import com.google.firebase.Timestamp
import java.util.Date
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatListScreen(
    viewModel: ChatViewModel = viewModel(),
    onNavigateToChatRoom: (ChatRoom) -> Unit,
    onNavigateToUserList: () -> Unit
) {
    val chatRooms by viewModel.chatRooms.collectAsStateWithLifecycle()
    val currentUserId = FirebaseAuth.getInstance().currentUser?.uid ?: ""

    // ✅ FIX: Add debug logging to see what's received
    LaunchedEffect(chatRooms, currentUserId) {
        println("🔥 CHAT LIST RENDER DEBUG:")
        println("  - Current User ID: '$currentUserId'")
        println("  - Number of chat rooms from ViewModel: ${chatRooms.size}")
        chatRooms.forEach { room ->
            println("  - Room ID: ${room.id}")
            println("    Participants in room: ${room.participants}")
            println("    Can find other participant: ${room.getOtherParticipantId(currentUserId).isNotBlank()}")
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Chats") },
                actions = {
                    IconButton(onClick = onNavigateToUserList) {
                        Icon(Icons.Default.Message, contentDescription = "New Chat")
                    }
                }
            )
        }
    ) { padding ->
        if (chatRooms.isEmpty()) {
            EmptyChatList(
                modifier = Modifier.padding(padding),
                onNavigateToUserList = onNavigateToUserList
            )
        } else {
            LazyColumn(
                modifier = Modifier
                    .padding(padding)
                    .fillMaxSize()
            ) {
                // ✅ FIX: Add explicit filtering and logging
                items(chatRooms) { chatRoom ->
                    val otherParticipantId = chatRoom.getOtherParticipantId(currentUserId)

                    if (otherParticipantId.isNotBlank()) {
                        ChatRoomItem(
                            chatRoom = chatRoom,
                            currentUserId = currentUserId,
                            onClick = { onNavigateToChatRoom(chatRoom) }
                        )
                    } else {
                        // This should never happen if data is correct
                        println("🚨 SKIPPING room ${chatRoom.id} - cannot find other participant for user $currentUserId")
                    }
                }
            }
        }
    }
}

@Composable
private fun ChatRoomItem(
    chatRoom: ChatRoom,
    currentUserId: String,
    onClick: () -> Unit
) {
    val otherParticipantId = chatRoom.getOtherParticipantId(currentUserId)
    val otherParticipantName = chatRoom.getOtherParticipantName(currentUserId)
    val otherParticipantPhoto = chatRoom.participantPhotos[otherParticipantId]
    val unreadCount = chatRoom.getUnreadCountForUser(currentUserId)
    val isUnread = unreadCount > 0

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 4.dp)
            .clickable { onClick() }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {

            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(CircleShape)
            ) {
                if (otherParticipantPhoto != null) {
                    AsyncImage(
                        model = otherParticipantPhoto,
                        contentDescription = "Profile picture",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Surface(
                        modifier = Modifier.fillMaxSize(),
                        color = MaterialTheme.colorScheme.primaryContainer
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                Icons.Default.Person,
                                contentDescription = null,
                                modifier = Modifier.size(32.dp)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = otherParticipantName,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = if (isUnread) FontWeight.Bold else FontWeight.Normal
                )

                Spacer(modifier = Modifier.height(2.dp))

                Text(
                    text = chatRoom.lastMessage,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    style = MaterialTheme.typography.bodySmall
                )
            }

            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = formatTime(chatRoom.lastMessageTime),
                    style = MaterialTheme.typography.labelSmall
                )

                if (unreadCount > 0) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Badge {
                        Text(unreadCount.toString())
                    }
                }
            }
        }
    }
}

@Composable
private fun EmptyChatList(
    modifier: Modifier = Modifier,
    onNavigateToUserList: () -> Unit
) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                Icons.Default.ChatBubbleOutline,
                contentDescription = null,
                modifier = Modifier.size(64.dp),
                tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.6f)
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "No conversations yet",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )

            Spacer(modifier = Modifier.height(8.dp))

            TextButton(onClick = onNavigateToUserList) {
                Text("Start a conversation")
            }
        }
    }
}

private fun formatTime(timestamp: Timestamp): String {
    val date = timestamp.toDate()
    val now = Date()
    val diff = now.time - date.time

    return when {
        diff < 60 * 1000 -> "Now"
        diff < 60 * 60 * 1000 -> "${diff / (60 * 1000)}m"
        diff < 24 * 60 * 60 * 1000 -> "${diff / (60 * 60 * 1000)}h"
        else -> SimpleDateFormat("dd/MM", Locale.getDefault()).format(date)
    }
}
