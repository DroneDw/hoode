package com.example.mybalaka.data

import com.example.mybalaka.model.MarketItem
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.Timestamp
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

object MarketRepository {
    private const val MARKET_ITEMS = "market_items"
    private val db = FirebaseFirestore.getInstance()

    // Add a new market item
    suspend fun addMarketItem(item: MarketItem): String {
        val data = hashMapOf(
            "title" to item.title,
            "description" to item.description,
            "price" to item.price,
            "category" to item.category,
            "imageUrl" to item.imageUrl,
            "sellerId" to item.sellerId,
            "sellerName" to item.sellerName,
            "phone" to item.phone,
            "whatsapp" to item.whatsapp,
            "location" to item.location,
            "isAvailable" to item.isAvailable, // ✅ FIX
            "approved" to item.approved,
            "createdAt" to Timestamp.now()
        )

        return db.collection(MARKET_ITEMS)
            .add(data)
            .await()
            .id
    }

    // Get all available market items
    fun getAvailableMarketItems(): Flow<List<MarketItem>> = callbackFlow {
        val registration = db.collection(MARKET_ITEMS)
            .whereEqualTo("isAvailable", true)
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                val items = snapshot?.documents?.mapNotNull { doc ->
                    try {
                        val title = doc.getString("title") ?: ""
                        val description = doc.getString("description") ?: ""
                        val price = doc.getDouble("price")?.toFloat() ?: 0f
                        val category = doc.getString("category") ?: ""
                        val imageUrl = doc.getString("imageUrl") ?: ""
                        val sellerId = doc.getString("sellerId") ?: ""
                        val sellerName = doc.getString("sellerName") ?: ""
                        val phone = doc.getString("phone") ?: ""
                        val whatsapp = doc.getString("whatsapp") ?: ""
                        val location = doc.getString("location") ?: "Balaka"
                        val isAvailable = doc.getBoolean("isAvailable") ?: true
                        val createdAt = doc.getTimestamp("createdAt")
                            ?: try {
                                val dateString = doc.getString("createdAt") ?: ""
                                Timestamp.now()
                            } catch (e: Exception) {
                                Timestamp.now()
                            }

                        MarketItem(
                            id = doc.id,
                            title = title,
                            description = description,
                            price = price,
                            category = category,
                            imageUrl = imageUrl,
                            sellerId = sellerId,
                            sellerName = sellerName,
                            phone = phone,
                            whatsapp = whatsapp,
                            location = location,
                            isAvailable = isAvailable,
                            createdAt = createdAt
                        )
                    } catch (e: Exception) {
                        println("Error mapping document: ${e.message}")
                        null
                    }
                } ?: emptyList()
                trySend(items)
            }
        awaitClose { registration.remove() }
    }

    // Get a single market item by ID
    suspend fun getMarketItemById(itemId: String): MarketItem? {
        return try {
            db.collection(MARKET_ITEMS)
                .document(itemId)
                .get()
                .await()
                .toObject(MarketItem::class.java)
        } catch (e: Exception) {
            null
        }
    }
}
