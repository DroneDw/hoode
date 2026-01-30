package com.example.mybalaka.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mybalaka.data.ChatRepository
import com.example.mybalaka.model.ChatRoom
import com.example.mybalaka.model.Message
import com.example.mybalaka.model.User
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class ChatViewModel : ViewModel() {

    private val repository = ChatRepository

    // ✅ FIX: Use dynamic getter that always gets the CURRENT user ID
    private val currentUserId: String
        get() = FirebaseAuth.getInstance().currentUser?.uid ?: ""

    // ✅ FIX: Add auth state listener to clear data when user changes
    private val authListener = FirebaseAuth.AuthStateListener { auth ->
        val newUserId = auth.currentUser?.uid
        println("🔍 Auth state changed. New user: $newUserId")

        // Clear all cached data when user changes
        if (newUserId == null || newUserId != _lastKnownUserId) {
            clearAllData()
            _lastKnownUserId = newUserId
        }
    }

    private var _lastKnownUserId: String? = null

    init {
        println("🚀 ChatViewModel initialized")
        println("🔍 Current user ID: '${currentUserId}'")

        // ✅ FIX: Register auth listener
        FirebaseAuth.getInstance().addAuthStateListener(authListener)

        // Set initial known user ID
        _lastKnownUserId = currentUserId
    }

    // UI State
    var selectedChatRoom by mutableStateOf<ChatRoom?>(null)
        private set

    var messageText by mutableStateOf("")
        private set

    var isLoading by mutableStateOf(false)
        private set

    // Flows
    val chatRooms: StateFlow<List<ChatRoom>> = flow {
        // ✅ FIX: Always use current user ID when collecting
        repository.getChatRoomsForUser(currentUserId).collect { emit(it) }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val allUsers: StateFlow<List<User>> = flow {
        // ✅ FIX: Always use current user ID when collecting
        repository.getAllRegisteredUsers(currentUserId).collect { emit(it) }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    private val _messages = MutableStateFlow<List<Message>>(emptyList())
    val messages: StateFlow<List<Message>> = _messages.asStateFlow()

    // StateFlow for provider chat rooms
    private val _providerChatRooms = MutableStateFlow<List<ChatRoom>>(emptyList())
    val providerChatRooms: StateFlow<List<ChatRoom>> = _providerChatRooms.asStateFlow()

    // ✅ FIX: Clear all cached data
    private fun clearAllData() {
        println("🧹 Clearing all ViewModel data due to user change")
        selectedChatRoom = null
        _messages.value = emptyList()
        _providerChatRooms.value = emptyList()
        messageText = ""
        isLoading = false
    }

    // Current chat room messages
    fun loadMessages(chatRoomId: String) {
        viewModelScope.launch {
            println("🔍 ChatViewModel: Starting to load messages for chatRoomId: $chatRoomId, currentUser: $currentUserId")

            // Clear existing messages first
            _messages.value = emptyList()

            // Load new messages
            repository.getMessagesForChatRoom(chatRoomId)
                .collect { messageList ->
                    println("🔍 ChatViewModel: Received ${messageList.size} messages for user $currentUserId")
                    _messages.value = messageList

                    // Mark messages as read
                    if (messageList.isNotEmpty()) {
                        markAsRead(chatRoomId)
                    }
                }
        }
    }

    // Send message
    fun sendMessage(receiverId: String) {
        if (messageText.isBlank()) return

        viewModelScope.launch {
            isLoading = true
            try {
                // ✅ FIX: Always use current user ID
                val senderId = currentUserId
                println("🔍 Sending message from $senderId to $receiverId")

                // Create or get chat room
                val chatRoomId = repository.createOrGetChatRoom(senderId, receiverId)

                // Create message
                val message = Message(
                    senderId = senderId,
                    receiverId = receiverId,
                    content = messageText.trim(),
                    timestamp = Timestamp.now(),
                    chatRoomId = chatRoomId
                )

                repository.sendMessage(message)
                messageText = ""

            } catch (e: Exception) {
                println("❌ Error sending message: ${e.message}")
                e.printStackTrace()
            } finally {
                isLoading = false
            }
        }
    }

    // Send message in existing chat room
// Send message in existing chat room
    fun sendMessageInChatRoom(chatRoomId: String, receiverId: String) {
        if (messageText.isBlank()) return

        viewModelScope.launch {
            isLoading = true
            try {
                val senderId = currentUserId
                println("🔍 Sending message in existing chat room from $senderId to $receiverId")

                // ✅ FIX: Ensure room exists before sending message
                val actualRoomId = repository.createOrGetChatRoom(senderId, receiverId)

                // If the room IDs don't match, something is wrong
                if (actualRoomId != chatRoomId) {
                    println("⚠️ Warning: Room ID mismatch! Expected: $chatRoomId, Got: $actualRoomId")
                }

                val message = Message(
                    senderId = senderId,
                    receiverId = receiverId,
                    content = messageText.trim(),
                    timestamp = Timestamp.now(),
                    chatRoomId = chatRoomId
                )

                repository.sendMessage(message)
                messageText = ""

                // Force reload messages after sending
                loadMessages(chatRoomId)

            } catch (e: Exception) {
                println("❌ Error sending message: ${e.message}")
                e.printStackTrace()
            } finally {
                isLoading = false
            }
        }
    }
    // Mark messages as read
    fun markAsRead(chatRoomId: String) {
        viewModelScope.launch {
            val userId = currentUserId
            println("🔍 Marking messages as read for user $userId in room: $chatRoomId")
            repository.markMessagesAsRead(chatRoomId, userId)
        }
    }

    // Set current chat room
    fun setCurrentChatRoom(chatRoom: ChatRoom) {
        selectedChatRoom = chatRoom
        loadMessages(chatRoom.id)
        markAsRead(chatRoom.id)
    }

    // Update message text
    fun updateMessageText(text: String) {
        messageText = text
    }

    // Clear current chat room
    fun clearCurrentChatRoom() {
        selectedChatRoom = null
        _messages.value = emptyList()
    }

    // Get or create chat room with user
    fun getOrCreateChatRoomWithUser(userId: String, onResult: (String) -> Unit) {
        viewModelScope.launch {
            try {
                val chatRoomId = repository.createOrGetChatRoom(currentUserId, userId)
                onResult(chatRoomId)
            } catch (e: Exception) {
                println("❌ Error creating/loading chat room: ${e.message}")
                e.printStackTrace()
            }
        }
    }

    // Load provider chat rooms
    fun loadProviderChatRooms(providerId: String) {
        viewModelScope.launch {
            println("🔍 Loading provider chat rooms for: $providerId")
            repository.getProviderChatRooms(providerId)
                .collect { chatRooms ->
                    println("✅ Provider chat rooms loaded: ${chatRooms.size}")
                    _providerChatRooms.value = chatRooms
                }
        }
    }

    // ✅ FIX: Clean up auth listener
    override fun onCleared() {
        super.onCleared()
        FirebaseAuth.getInstance().removeAuthStateListener(authListener)
    }
}