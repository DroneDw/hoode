package com.example.mybalaka.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.Exclude
import com.google.firebase.firestore.IgnoreExtraProperties
import com.google.firebase.firestore.PropertyName

@IgnoreExtraProperties
data class Event(
    val id: String = "",
    val title: String = "",
    val description: String = "",
    val venue: String = "",
    val date: Timestamp = Timestamp.now(),
    val endDate: Timestamp? = null,

    // 🔥 Firestore-aligned fields
    val multiDay: Boolean = false,

    // ✅ UPDATED: Field-level annotation for Firestore constructor mapping
    @field:PropertyName("ticketed")
    val isTicketed: Boolean = false,

    val recurrence: String = "None",
    val posterUrl: String = "",
    val contactPhone: String = "",

    // ⛔ OLD (keep for old data)
    val organiserId: String = "",

    // ✅ NEW (ADMIN assigns organisers - multi)
    val organiserIds: List<String> = emptyList(),

    // ✅ NEW (normalized single organizer – DO NOT REMOVE OLD)
    val organizerId: String = "",
    val organizerName: String = "",

    var likes: Int = 0,
    val likedBy: List<String> = emptyList(),

    // 🔥 Allow old + new comment formats safely
    val comments: List<Any> = emptyList(),

    val category: String = "Other",
    val ticketTypes: List<TicketType> = emptyList()
) {

    /* -------------------------------------------------
       ✅ BACKWARD COMPATIBILITY (DO NOT REMOVE)
    --------------------------------------------------*/

    // Alias for multiDay (optional getter)
    @get:Exclude
    val isMultiDay: Boolean
        get() = multiDay

    // 🔥 BACKWARD SUPPORT FOR OLD `ticketed` FIELD
    @get:Exclude
    val ticketed: Boolean
        get() = isTicketed

    /* -------------------------------------------------
       ✅ Normalize comments safely
    --------------------------------------------------*/
    fun getNormalizedComments(): List<EventComment> {
        return comments.mapNotNull { item ->
            when (item) {
                is EventComment -> item
                is String -> EventComment(
                    authorId = "unknown",
                    authorName = "Anonymous",
                    text = item,
                    timestamp = Timestamp.now()
                )
                is Map<*, *> -> EventComment(
                    authorId = item["authorId"] as? String ?: "",
                    authorName = item["authorName"] as? String ?: "Anonymous",
                    text = item["text"] as? String ?: "",
                    timestamp = item["timestamp"] as? Timestamp ?: Timestamp.now()
                )
                else -> null
            }
        }
    }
}