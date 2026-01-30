package com.example.mybalaka.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.IgnoreExtraProperties

@IgnoreExtraProperties
data class Message(
    @DocumentId
    val id: String = "",
    val senderId: String = "",
    val receiverId: String = "",
    val content: String = "",
    val timestamp: Timestamp = Timestamp.now(),
    val isRead: Boolean = false,
    val chatRoomId: String = "",
    val bookingId: String = "" // Added this line to fix unbook reference
) {
    // Helper function to determine if this message belongs to current user
    fun isSentByCurrentUser(currentUserId: String): Boolean = senderId == currentUserId

    // Helper function to get the other participant in the chat
    fun getOtherParticipant(currentUserId: String): String {
        return if (senderId == currentUserId) receiverId else senderId
    }
}
