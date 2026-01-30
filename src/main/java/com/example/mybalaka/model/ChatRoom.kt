package com.example.mybalaka.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.IgnoreExtraProperties

@IgnoreExtraProperties
data class ChatRoom(
    // ✅ FIX: Remove @DocumentId - we'll set this manually
    val id: String = "",
    val participants: List<String> = emptyList(),
    val participantNames: Map<String, String> = emptyMap(),
    val participantPhotos: Map<String, String?> = emptyMap(),
    val lastMessage: String = "",
    val lastMessageTime: Timestamp = Timestamp.now(),
    val lastMessageSenderId: String = "",
    val unreadCount: Map<String, Int> = emptyMap(),
    val createdAt: Timestamp = Timestamp.now()
) {

    /** ✅ SAFE: Returns empty string if not found */
    fun getOtherParticipantId(currentUserId: String): String {
        return participants.firstOrNull { it != currentUserId } ?: ""
    }

    /** ✅ SAFE: Returns "Unknown" if not found */
    fun getOtherParticipantName(currentUserId: String): String {
        val otherId = getOtherParticipantId(currentUserId)
        return if (otherId.isBlank()) "Unknown" else participantNames[otherId] ?: "Unknown"
    }

    fun getUnreadCountForUser(userId: String): Int {
        return unreadCount[userId] ?: 0
    }
}
