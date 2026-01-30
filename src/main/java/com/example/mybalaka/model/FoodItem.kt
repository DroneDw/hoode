package com.example.mybalaka.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId

data class FoodItem(
    @DocumentId
    val id: String = "",
    val name: String = "",
    val description: String = "",
    val price: Float = 0f,
    val category: String = "",
    val imageUrl: String = "",
    val cookId: String = "",
    val cookName: String = "",
    val phone: String = "",
    val whatsapp: String = "",
    val preparationTime: String = "",
    val isAvailable: Boolean = true,
    val createdAt: Timestamp = Timestamp.now(),
    val vendorType: String = "individual", // "restaurant", "shop", "individual"
    val isNew: Boolean = true // New items get tagged
)