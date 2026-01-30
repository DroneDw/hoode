package com.example.mybalaka.data

import com.example.mybalaka.model.ChatRoom
import com.example.mybalaka.model.Message
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

object ChatRepository {

    private const val CHAT_ROOMS = "chat_rooms_balaka"
    private const val MESSAGES = "messages_balaka"
    private const val USERS = "users"

    private val db = FirebaseFirestore.getInstance()

    // Create or get existing chat room between two users
    suspend fun createOrGetChatRoom(userId1: String, userId2: String): String {
        try {
            // Ensure consistent ordering for chat room ID
            val (firstId, secondId) = if (userId1 < userId2) userId1 to userId2 else userId2 to userId1
            val chatRoomId = "${firstId}_$secondId"

            val chatRoomRef = db.collection(CHAT_ROOMS).document(chatRoomId)
            val chatRoomDoc = chatRoomRef.get().await()

            if (!chatRoomDoc.exists()) {
                // Get user details for both participants
                val user1Doc = db.collection(USERS).document(userId1).get().await()
                val user2Doc = db.collection(USERS).document(userId2).get().await()

                val participantNames = mapOf(
                    userId1 to (user1Doc.getString("name") ?: "Unknown"),
                    userId2 to (user2Doc.getString("name") ?: "Unknown")
                )

                val participantPhotos = mapOf(
                    userId1 to user1Doc.getString("photoUrl"),
                    userId2 to user2Doc.getString("photoUrl")
                )

                // ✅ FIX: DO NOT include 'id' field in the document data
                val chatRoomData = mapOf(
                    // "id" to chatRoomId, // ❌ REMOVE THIS LINE
                    "participants" to listOf(userId1, userId2), // Explicit array
                    "participantNames" to participantNames,
                    "participantPhotos" to participantPhotos,
                    "lastMessage" to "",
                    "lastMessageTime" to Timestamp.now(),
                    "lastMessageSenderId" to "",
                    "unreadCount" to emptyMap<String, Int>(),
                    "createdAt" to Timestamp.now()
                )

                chatRoomRef.set(chatRoomData).await()
                println("✅ Chat room created with ID: $chatRoomId")
            }

            return chatRoomId
        } catch (e: Exception) {
            println("❌ Error in createOrGetChatRoom: ${e.message}")
            throw e
        }
    }

    // Send message
    suspend fun sendMessage(message: Message) {
        try {
            println("🔍 Attempting to send message: '${message.content}' in room: ${message.chatRoomId}")

            // ✅ FIX: First, ensure the chat room EXISTS before trying to update it
            val chatRoomRef = db.collection(CHAT_ROOMS).document(message.chatRoomId)
            val chatRoomDoc = chatRoomRef.get().await()

            if (!chatRoomDoc.exists()) {
                println("❌ Chat room doesn't exist! Creating it first...")

                // Extract participant IDs from chatRoomId (format: user1_user2)
                val participantIds = message.chatRoomId.split("_")
                if (participantIds.size == 2) {
                    createOrGetChatRoom(participantIds[0], participantIds[1])
                    println("✅ Chat room created: ${message.chatRoomId}")
                } else {
                    println("❌ ERROR: Could not parse participant IDs from ${message.chatRoomId}")
                    return // Don't proceed if we can't create the room
                }
            }

            // ✅ Now safe to add the message
            db.collection(MESSAGES).add(message).await()
            println("✅ Message added to messages_balaka")

            // ✅ Now safe to update the chat room
            chatRoomRef.update(mapOf(
                "lastMessage" to message.content,
                "lastMessageTime" to message.timestamp,
                "lastMessageSenderId" to message.senderId
            )).await()
            println("✅ Chat room updated with last message")

            // Update unread counts for receiver
            val participants = chatRoomDoc.get("participants") as? List<String>
                ?: message.chatRoomId.split("_") // Fallback if doc didn't exist

            val uniqueParticipants = participants.distinct()
            uniqueParticipants.forEach { participantId ->
                if (participantId != message.senderId) {
                    chatRoomRef.update(
                        "unreadCount.${participantId}",
                        FieldValue.increment(1)
                    )
                }
            }
            println("✅ Unread counts updated")

        } catch (e: Exception) {
            println("❌ Error in sendMessage: ${e.message}")
            e.printStackTrace()
            throw e
        }
    }

    // Get all chat rooms for a user
    fun getChatRoomsForUser(userId: String): Flow<List<ChatRoom>> = callbackFlow {
        try {
            println("🔍 Querying chat rooms for user: $userId")

            val registration = db.collection(CHAT_ROOMS)
                .whereArrayContains("participants", userId)
                .orderBy("lastMessageTime", Query.Direction.DESCENDING)
                .addSnapshotListener { snapshot, error ->
                    if (error != null) {
                        println("❌ Firestore Error in getChatRoomsForUser: ${error.message}")
                        close(error)
                        return@addSnapshotListener
                    }

                    val chatRooms = snapshot?.documents?.mapNotNull { doc ->
                        // ✅ FIX: Manually set the ID from document reference
                        doc.toObject(ChatRoom::class.java)?.copy(id = doc.id)
                    } ?: emptyList()

                    println("✅ Found ${chatRooms.size} chat rooms for user $userId")
                    chatRooms.forEach {
                        println("  - Room: ${it.id}, Participants: ${it.participants}")
                    }

                    trySend(chatRooms)
                }
            awaitClose { registration.remove() }
        } catch (e: Exception) {
            println("❌ Exception in getChatRoomsForUser: ${e.message}")
            close(e)
        }
    }

    // Get messages for a chat room
    fun getMessagesForChatRoom(chatRoomId: String): Flow<List<Message>> = callbackFlow {
        try {
            val registration = db.collection(MESSAGES)
                .whereEqualTo("chatRoomId", chatRoomId)
                .orderBy("timestamp", Query.Direction.ASCENDING)
                .addSnapshotListener { snapshot, error ->
                    if (error != null) {
                        println("❌ Firestore Error in getMessagesForChatRoom: ${error.message}")
                        close(error)
                        return@addSnapshotListener
                    }

                    val messages = snapshot?.documents?.mapNotNull { doc ->
                        doc.toObject(Message::class.java)?.copy(id = doc.id)
                    } ?: emptyList()

                    println("✅ Loaded ${messages.size} messages for room $chatRoomId")
                    trySend(messages)
                }
            awaitClose { registration.remove() }
        } catch (e: Exception) {
            println("❌ Exception in getMessagesForChatRoom: ${e.message}")
            close(e)
        }
    }

    // Mark messages as read
    suspend fun markMessagesAsRead(chatRoomId: String, userId: String) {
        try {
            // Reset unread count for this user in chat room
            db.collection(CHAT_ROOMS).document(chatRoomId)
                .update("unreadCount.$userId", 0)

            // Mark individual messages as read
            val messages = db.collection(MESSAGES)
                .whereEqualTo("chatRoomId", chatRoomId)
                .whereEqualTo("receiverId", userId)
                .whereEqualTo("isRead", false)
                .get()
                .await()

            messages.documents.forEach { doc ->
                doc.reference.update("isRead", true)
            }
            println("✅ Marked messages as read for user $userId in room $chatRoomId")
        } catch (e: Exception) {
            println("❌ Error in markMessagesAsRead: ${e.message}")
            throw e
        }
    }

    // Get ALL registered users for chat list (regardless of role)
    fun getAllRegisteredUsers(currentUserId: String): Flow<List<com.example.mybalaka.model.User>> = callbackFlow {
        try {
            println("🔍 Fetching all users except $currentUserId")

            val registration = db.collection(USERS)
                .addSnapshotListener { snapshot, error ->
                    if (error != null) {
                        println("❌ Error fetching users: ${error.message}")
                        close(error)
                        return@addSnapshotListener
                    }

                    val users = snapshot?.documents
                        ?.filter { it.id != currentUserId }
                        ?.mapNotNull { doc ->
                            val user = com.example.mybalaka.model.User(
                                id = doc.id,
                                name = doc.getString("name") ?: "Unknown",
                                email = doc.getString("email") ?: "",
                                role = doc.getString("role") ?: "USER",
                                phone = doc.getString("phone") ?: "",
                                photoUrl = doc.getString("photoUrl")
                            )
                            user
                        } ?: emptyList()

                    println("✅ Total users found: ${users.size}")
                    trySend(users)
                }

            awaitClose { registration.remove() }
        } catch (e: Exception) {
            println("❌ Exception in getAllRegisteredUsers: ${e.message}")
            close(e)
        }
    }

    // Get chat rooms for a provider
    fun getProviderChatRooms(providerId: String): Flow<List<ChatRoom>> = callbackFlow {
        try {
            val registration = db.collection(CHAT_ROOMS)
                .whereArrayContains("participants", providerId)
                .orderBy("lastMessageTime", Query.Direction.DESCENDING)
                .addSnapshotListener { snapshot, error ->
                    if (error != null) {
                        println("❌ Firestore Error in getProviderChatRooms: ${error.message}")
                        close(error)
                        return@addSnapshotListener
                    }

                    val rooms = snapshot?.documents?.mapNotNull { doc ->
                        // ✅ FIX: Manually set the ID from document reference
                        doc.toObject(ChatRoom::class.java)?.copy(id = doc.id)
                    } ?: emptyList()

                    println("✅ Found ${rooms.size} provider chat rooms")
                    trySend(rooms)
                }

            awaitClose { registration.remove() }
        } catch (e: Exception) {
            println("❌ Exception in getProviderChatRooms: ${e.message}")
            close(e)
        }
    }
}
