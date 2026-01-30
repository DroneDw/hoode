package com.example.mybalaka.screens.chat

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.example.mybalaka.model.ChatRoom
import com.example.mybalaka.viewmodel.ChatViewModel
import com.google.firebase.auth.FirebaseAuth

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProviderChatListScreen(
    navController: NavHostController,
    viewModel: ChatViewModel = viewModel()
) {
    val context = LocalContext.current
    val currentUserId = FirebaseAuth.getInstance().currentUser?.uid ?: ""
    val chatRooms by viewModel.providerChatRooms.collectAsState() // Updated to use providerChatRooms

    LaunchedEffect(currentUserId) {
        viewModel.loadProviderChatRooms(currentUserId)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Customer Chats") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        if (chatRooms.isEmpty()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("No chats yet", style = MaterialTheme.typography.headlineSmall)
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
            ) {
                items(chatRooms) { chatRoom ->
                    ProviderChatItem(chatRoom, navController)
                }
            }
        }
    }
}

@Composable
fun ProviderChatItem(
    chatRoom: ChatRoom,
    navController: NavHostController
) {
    val currentUserId = FirebaseAuth.getInstance().currentUser?.uid ?: ""
    val otherParticipantId = chatRoom.getOtherParticipantId(currentUserId)
    val otherParticipantName = chatRoom.getOtherParticipantName(currentUserId)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        onClick = {
            navController.navigate("chat_room/${chatRoom.id}/$otherParticipantId/$otherParticipantName")
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(otherParticipantName, style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(4.dp))
            Text(chatRoom.lastMessage.ifEmpty { "No messages yet" }, style = MaterialTheme.typography.bodyMedium)
        }
    }
}
