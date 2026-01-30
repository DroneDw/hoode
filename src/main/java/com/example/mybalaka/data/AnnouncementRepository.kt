package com.example.mybalaka.data

import com.example.mybalaka.model.Announcement
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

object AnnouncementRepository {

    private const val ANNOUNCEMENTS = "announcements"
    private val db = FirebaseFirestore.getInstance()

    fun getAnnouncements(currentUserId: String): Flow<List<Announcement>> = callbackFlow {
        val registration = db.collection(ANNOUNCEMENTS)
            .orderBy("createdAt", com.google.firebase.firestore.Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }

                val now = Timestamp.now()

                val announcements = snapshot?.documents
                    ?.mapNotNull { doc ->
                        doc.toObject(Announcement::class.java)?.copy(id = doc.id)
                    }
                    // 🔥 REMOVE EXPIRED
                    ?.filter { it.expiresAt == null || it.expiresAt > now }
                    // 🔥 UNREAD FIRST
                    ?.sortedWith(
                        compareBy<Announcement> {
                            currentUserId in it.readBy
                        }.thenByDescending { it.createdAt }
                    )
                    ?: emptyList()

                trySend(announcements)
            }

        awaitClose { registration.remove() }
    }

    suspend fun addAnnouncement(announcement: Announcement) {
        db.collection(ANNOUNCEMENTS)
            .add(announcement)
            .await()
    }

    suspend fun markAsRead(announcementId: String, userId: String) {
        db.collection(ANNOUNCEMENTS)
            .document(announcementId)
            .update("readBy", FieldValue.arrayUnion(userId))
            .await()
    }
}
