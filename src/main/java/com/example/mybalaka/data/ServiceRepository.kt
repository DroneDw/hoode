package com.example.mybalaka.data

import com.example.mybalaka.model.Service
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await // Keep for getProviderName

object ServiceRepository {
    private const val COLLECTION = "services"
    private val db = FirebaseFirestore.getInstance()

    fun getServicesSortedByRating(): Flow<List<Service>> = callbackFlow {
        // FIXED: Capture listener registration return value, not collection reference
        val registration = db.collection(COLLECTION)
            .addSnapshotListener { snap, error ->
                if (error != null) { close(error); return@addSnapshotListener }
                if (snap == null || snap.isEmpty) { trySend(emptyList()); return@addSnapshotListener }

                val serviceList = mutableListOf<Service>()
                var completed = 0

                for (doc in snap.documents) {
                    val base = Service(
                        id = doc.id,
                        title = doc.getString("title") ?: "",
                        description = doc.getString("description") ?: "",
                        category = doc.getString("category") ?: "",
                        phone = doc.getString("phone") ?: "",
                        whatsapp = doc.getString("whatsapp") ?: "",
                        price = doc.getString("price") ?: "",
                        posterUrl = doc.getString("posterUrl") ?: "",
                        providerId = doc.getString("providerId") ?: "",
                        createdAt = doc.getTimestamp("createdAt") ?: Timestamp.now(),
                        isAvailable = doc.getBoolean("isAvailable") ?: true,
                        bookedBy = doc.getString("bookedBy")
                    )
                    serviceList.add(base)

                    // Attach live average
                    doc.reference.collection("ratings")
                        .addSnapshotListener { ratSnap, _ ->
                            val ratings = ratSnap?.documents?.mapNotNull { it.getDouble("value")?.toFloat() } ?: emptyList()
                            base.rating = if (ratings.isEmpty()) 0f else ratings.average().toFloat()
                            base.voteCount = ratings.size

                            if (++completed == snap.size()) {
                                trySend(serviceList.sortedByDescending { it.rating })
                            }
                        }
                }
            }

        // FIXED: Call remove() on the listener registration
        awaitClose { registration.remove() }
    }

    suspend fun rateService(serviceId: String, userId: String, stars: Float) {
        db.collection(COLLECTION)
            .document(serviceId)
            .collection("ratings")
            .document(userId)
            .set(mapOf("value" to stars, "timestamp" to Timestamp.now()))
    }

    suspend fun addService(service: Service) {
        db.collection(COLLECTION).add(service)
    }

    suspend fun createBooking(service: Service, customerId: String) {
        db.collection(COLLECTION).document(service.id)
            .update(mapOf(
                "isAvailable" to false,
                "bookedBy" to customerId
            ))
    }

    // This suspend function is correct - call it from viewModelScope.launch {}
    suspend fun getProviderName(providerId: String): String {
        return try {
            val doc = db.collection("users").document(providerId).get().await()
            doc.getString("name") ?: "Provider"
        } catch (e: Exception) {
            "Provider"
        }
    }
}