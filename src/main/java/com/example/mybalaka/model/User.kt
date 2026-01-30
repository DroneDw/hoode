package com.example.mybalaka.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.IgnoreExtraProperties

@IgnoreExtraProperties
data class User(
    @DocumentId
    val id: String = "",
    val name: String = "",
    val email: String = "",
    val phone: String = "",
    val role: String = UserRole.USER.name, // Key field for authorization
    val photoUrl: String? = null,
    val isProvider: Boolean = false, // Existing field

    // ✅ used for unread announcements count
    val lastSeenAnnouncementAt: Timestamp? = null
)

enum class UserRole {
    USER,
    SELLER,      // Product sellers
    PROVIDER,    // Service providers
    ORGANIZER,   // ✅ Event organizers (NEW)
    ADMIN
}
