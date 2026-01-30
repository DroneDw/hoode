package com.example.mybalaka.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.PropertyName

data class Property(
    @DocumentId
    val id: String = "",

    val title: String = "",
    val description: String = "",
    val price: Float = 0f,
    val category: String = "",
    val location: String = "Balaka",
    val imageUrls: List<String> = emptyList(),

    val sellerId: String = "",
    val sellerName: String = "",
    val phone: String = "",
    val whatsapp: String = "",

    @get:PropertyName("available")
    @set:PropertyName("available")
    var isAvailable: Boolean = true,

    val createdAt: Timestamp = Timestamp.now(),
    val propertyType: String = "rental"
)
