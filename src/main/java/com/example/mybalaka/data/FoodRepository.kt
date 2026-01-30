package com.example.mybalaka.data

import com.example.mybalaka.model.FoodItem
import com.example.mybalaka.model.FoodOrder
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

object FoodRepository {
    private const val FOOD_ITEMS = "food_items_balaka"
    private const val FOOD_ORDERS = "food_orders_balaka"
    private val db = FirebaseFirestore.getInstance()

    suspend fun addFoodItem(item: FoodItem): String {
        val data = hashMapOf(
            "name" to item.name,
            "description" to item.description,
            "price" to item.price,
            "category" to item.category,
            "imageUrl" to item.imageUrl,
            "cookId" to item.cookId,
            "cookName" to item.cookName,
            "phone" to item.phone,
            "whatsapp" to item.whatsapp,
            "preparationTime" to item.preparationTime,
            "isAvailable" to item.isAvailable,
            "createdAt" to Timestamp.now(),
            // ✅ FIXED: Added missing vendor type and isNew fields
            "vendorType" to item.vendorType,
            "isNew" to item.isNew
        )

        return db.collection(FOOD_ITEMS)
            .add(data)
            .await()
            .id
    }

    fun getAvailableFoodItems(): Flow<List<FoodItem>> = callbackFlow {
        val registration = db.collection(FOOD_ITEMS)
            .whereEqualTo("isAvailable", true)
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                val items = snapshot?.documents?.mapNotNull { doc ->
                    try {
                        FoodItem(
                            id = doc.id,
                            name = doc.getString("name") ?: "",
                            description = doc.getString("description") ?: "",
                            price = doc.getDouble("price")?.toFloat() ?: 0f,
                            category = doc.getString("category") ?: "",
                            imageUrl = doc.getString("imageUrl") ?: "",
                            cookId = doc.getString("cookId") ?: "",
                            cookName = doc.getString("cookName") ?: "",
                            phone = doc.getString("phone") ?: "",
                            whatsapp = doc.getString("whatsapp") ?: "",
                            preparationTime = doc.getString("preparationTime") ?: "",
                            isAvailable = doc.getBoolean("isAvailable") ?: true,
                            createdAt = doc.getTimestamp("createdAt") ?: Timestamp.now(),
                            // ✅ FIXED: Added missing vendor type and isNew mappings
                            vendorType = doc.getString("vendorType") ?: "individual",
                            isNew = doc.getBoolean("isNew") ?: false
                        )
                    } catch (e: Exception) {
                        println("Error mapping food item: ${e.message}")
                        null
                    }
                } ?: emptyList()
                trySend(items)
            }
        awaitClose { registration.remove() }
    }

    fun getFoodItemsByCook(cookId: String): Flow<List<FoodItem>> = callbackFlow {
        val registration = db.collection(FOOD_ITEMS)
            .whereEqualTo("cookId", cookId)
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                val items = snapshot?.documents?.mapNotNull { doc ->
                    doc.toObject(FoodItem::class.java)?.copy(id = doc.id)
                } ?: emptyList()
                trySend(items)
            }
        awaitClose { registration.remove() }
    }

    suspend fun createOrder(order: FoodOrder): String {
        val data = hashMapOf(
            "customerId" to order.customerId,
            "customerName" to order.customerName,
            "customerPhone" to order.customerPhone,
            "cookId" to order.cookId,
            "cookName" to order.cookName,
            "items" to order.items,
            "totalAmount" to order.totalAmount,
            "status" to order.status,
            "deliveryAddress" to order.deliveryAddress,
            "notes" to order.notes,
            "createdAt" to Timestamp.now(),
            "updatedAt" to Timestamp.now(),
            // FIXED: Added payment fields
            "senderName" to order.senderName,
            "amountSent" to order.amountSent,
            "paymentMethod" to order.paymentMethod,
            "paymentReceived" to order.paymentReceived,
            "paymentConfirmedAt" to order.paymentConfirmedAt,
            // Added vendor type
            "vendorType" to "restaurant"
        )

        return db.collection(FOOD_ORDERS)
            .add(data)
            .await()
            .id
    }

    suspend fun updateOrderPaymentStatus(orderId: String, received: Boolean) {
        db.collection(FOOD_ORDERS)
            .document(orderId)
            .update(
                mapOf(
                    "paymentReceived" to received,
                    "status" to if (received) "payment_received" else "pending",
                    "paymentConfirmedAt" to if (received) Timestamp.now() else null,
                    "updatedAt" to Timestamp.now()
                )
            )
            .await()
    }

    fun getCustomerOrders(customerId: String): Flow<List<FoodOrder>> = callbackFlow {
        val registration = db.collection(FOOD_ORDERS)
            .whereEqualTo("customerId", customerId)
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                val orders = snapshot?.documents?.mapNotNull { doc ->
                    doc.toObject(FoodOrder::class.java)?.copy(id = doc.id)
                } ?: emptyList()
                trySend(orders)
            }
        awaitClose { registration.remove() }
    }

    fun getCookOrders(cookId: String): Flow<List<FoodOrder>> = callbackFlow {
        val registration = db.collection(FOOD_ORDERS)
            .whereEqualTo("cookId", cookId)
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                val orders = snapshot?.documents?.mapNotNull { doc ->
                    doc.toObject(FoodOrder::class.java)?.copy(id = doc.id)
                } ?: emptyList()
                trySend(orders)
            }
        awaitClose { registration.remove() }
    }

    suspend fun updateOrderStatus(orderId: String, newStatus: String) {
        db.collection(FOOD_ORDERS)
            .document(orderId)
            .update(
                mapOf(
                    "status" to newStatus,
                    "updatedAt" to Timestamp.now()
                )
            )
            .await()
    }

    suspend fun updateFoodItemAvailability(itemId: String, isAvailable: Boolean) {
        db.collection(FOOD_ITEMS)
            .document(itemId)
            .update("isAvailable", isAvailable)
            .await()
    }
}