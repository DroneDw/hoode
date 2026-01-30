package com.example.mybalaka.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.PropertyName

data class MarketItem(
    @DocumentId
    val id: String = "",
    val title: String = "",
    val description: String = "",
    val price: Float = 0f,
    val category: String = "",
    val imageUrl: String = "",
    val sellerId: String = "",
    val sellerName: String = "",
    val phone: String = "",
    val whatsapp: String = "",
    val location: String = "Balaka",

    // ✅ FIX: Force Firestore to use "isAvailable" as the field name
    @get:PropertyName("isAvailable")
    @set:PropertyName("isAvailable")
    var isAvailable: Boolean = true,

    // Admin approval control
    val approved: Boolean = false,

    val createdAt: Timestamp = Timestamp.now()
)