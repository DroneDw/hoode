package com.example.mybalaka.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.ServerTimestamp
import java.util.Date

// In model/FoodOrder.kt
data class FoodOrder(
    val id: String = "",
    val customerId: String = "",
    val customerName: String = "",
    val customerPhone: String = "",
    val cookId: String = "",
    val cookName: String = "",
    val items: List<OrderItem> = emptyList(),
    val totalAmount: Float = 0f,
    val deliveryAddress: String = "",
    val notes: String = "",
    val status: String = "pending",
    @ServerTimestamp
    val createdAt: com.google.firebase.Timestamp? = null,
    //  NEW: Manual payment verification
    val senderName: String = "",
    val amountSent: Float = 0f,
    val paymentMethod: String = "",
    val paymentReceived: Boolean = false,
    val paymentConfirmedAt: com.google.firebase.Timestamp? = null
)

data class OrderItem(
    val itemId: String = "",
    val name: String = "",
    val quantity: Int = 0,
    val price: Float = 0f
)