package com.example.mybalaka.data

import com.example.mybalaka.model.Property
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.Timestamp
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

object PropertyRepository {
    private const val PROPERTIES = "properties_balaka"
    private val db = FirebaseFirestore.getInstance()

    fun getAvailableProperties(): Flow<List<Property>> = callbackFlow {
        val registration = db.collection(PROPERTIES)
            // ✅ FIX: query Firestore field "available"
            .whereEqualTo("available", true)
            .orderBy("createdAt", com.google.firebase.firestore.Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }

                val properties = snapshot?.documents?.mapNotNull { doc ->
                    try {
                        Property(
                            id = doc.id,
                            title = doc.getString("title") ?: "",
                            description = doc.getString("description") ?: "",
                            price = doc.getDouble("price")?.toFloat() ?: 0f,
                            category = doc.getString("category") ?: "",
                            location = doc.getString("location") ?: "Balaka",
                            imageUrls = doc.get("imageUrls") as? List<String> ?: emptyList(),
                            sellerId = doc.getString("sellerId") ?: "",
                            sellerName = doc.getString("sellerName") ?: "",
                            phone = doc.getString("phone") ?: "",
                            whatsapp = doc.getString("whatsapp") ?: "",
                            // ✅ FIX: read Firestore field "available"
                            isAvailable = doc.getBoolean("available") ?: true,
                            createdAt = doc.getTimestamp("createdAt") ?: Timestamp.now(),
                            propertyType = doc.getString("propertyType") ?: "rental"
                        )
                    } catch (e: Exception) {
                        null
                    }
                } ?: emptyList()

                trySend(properties)
            }

        awaitClose { registration.remove() }
    }

    suspend fun addProperty(property: Property): String {
        val data = hashMapOf(
            "title" to property.title,
            "description" to property.description,
            "price" to property.price,
            "category" to property.category,
            "location" to property.location,
            "imageUrls" to property.imageUrls,
            "sellerId" to property.sellerId,
            "sellerName" to property.sellerName,
            "phone" to property.phone,
            "whatsapp" to property.whatsapp,
            // ✅ FIX: save as "available"
            "available" to property.isAvailable,
            "createdAt" to Timestamp.now(),
            "propertyType" to property.propertyType
        )

        return db.collection(PROPERTIES).add(data).await().id
    }

    suspend fun getPropertyById(propertyId: String): Property? {
        return try {
            db.collection(PROPERTIES)
                .document(propertyId)
                .get()
                .await()
                .toObject(Property::class.java)
        } catch (e: Exception) {
            null
        }
    }
}
