package com.example.mybalaka.data

import com.example.mybalaka.model.Event
import com.example.mybalaka.model.EventComment
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

object EventRepository {

    private const val COLLECTION = "events_balaka"
    private val db = FirebaseFirestore.getInstance()

    fun getEvents(): Flow<List<Event>> = callbackFlow {
        val registration = db.collection(COLLECTION)
            .orderBy("date")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }

                val events = snapshot?.documents?.mapNotNull { doc ->
                    val event = doc.toObject(Event::class.java)?.copy(id = doc.id)
                    val rawComments = doc.get("comments") as? List<Any>

                    val mappedComments = rawComments?.mapNotNull { item ->
                        when (item) {
                            is EventComment -> item
                            is Map<*, *> -> EventComment(
                                authorId = item["authorId"] as? String ?: "",
                                authorName = item["authorName"] as? String ?: "Anonymous",
                                text = item["text"] as? String ?: "",
                                timestamp = item["timestamp"] as? Timestamp ?: Timestamp.now()
                            )
                            is String -> EventComment(
                                authorId = "unknown",
                                authorName = "Anonymous",
                                text = item,
                                timestamp = Timestamp.now()
                            )
                            else -> null
                        }
                    } ?: emptyList()

                    event?.copy(comments = mappedComments)
                } ?: emptyList()

                trySend(events)
            }

        awaitClose { registration.remove() }
    }

    suspend fun addEvent(event: Event) {
        db.collection(COLLECTION).add(event).await()
    }

    suspend fun getEvent(eventId: String): Event? {
        val doc = db.collection(COLLECTION).document(eventId).get().await()
        val event = doc.toObject(Event::class.java)?.copy(id = doc.id)
        val rawComments = doc.get("comments") as? List<Any>

        val mappedComments = rawComments?.mapNotNull { item ->
            when (item) {
                is EventComment -> item
                is Map<*, *> -> EventComment(
                    authorId = item["authorId"] as? String ?: "",
                    authorName = item["authorName"] as? String ?: "Anonymous",
                    text = item["text"] as? String ?: "",
                    timestamp = item["timestamp"] as? Timestamp ?: Timestamp.now()
                )
                is String -> EventComment(
                    authorId = "unknown",
                    authorName = "Anonymous",
                    text = item,
                    timestamp = Timestamp.now()
                )
                else -> null
            }
        } ?: emptyList()

        return event?.copy(comments = mappedComments)
    }

    suspend fun likeEvent(eventId: String, userId: String) {
        val eventRef = db.collection(COLLECTION).document(eventId)
        val event = getEvent(eventId) ?: return

        if (!event.likedBy.contains(userId)) {
            eventRef.update(
                mapOf(
                    "likes" to event.likes + 1,
                    "likedBy" to FieldValue.arrayUnion(userId)
                )
            ).await()
        }
    }

    suspend fun commentOnEvent(eventId: String, comment: EventComment) {
        db.collection(COLLECTION).document(eventId)
            .update(
                "comments",
                FieldValue.arrayUnion(
                    mapOf(
                        "authorId" to comment.authorId,
                        "authorName" to comment.authorName,
                        "text" to comment.text,
                        "timestamp" to comment.timestamp
                    )
                )
            ).await()
    }

    // ================= ORGANIZER: GET EVENTS BY ASSIGNED ORGANIZER =================
    // ✅ CORRECTED: Uses "organiserIds" to match Kotlin property name
    fun getEventsByOrganizer(organizerId: String): Flow<List<Event>> = callbackFlow {
        val registration = db.collection(COLLECTION)
            .whereArrayContains("organiserIds", organizerId)  // 🔴 CRITICAL FIX: Must match model spelling
            .orderBy("date")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }

                val events = snapshot?.documents?.mapNotNull { doc ->
                    doc.toObject(Event::class.java)?.copy(id = doc.id)
                } ?: emptyList()

                trySend(events)
            }

        awaitClose { registration.remove() }
    }
}