package com.example.mybalaka.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId

data class SellerRequest(
    @DocumentId
    val id: String = "",
    val userId: String = "",
    val fullName: String = "",
    val phone: String = "",
    val nationalIdFront: String = "",
    val nationalIdBack: String = "",
    val selfieImage: String = "",
    val status: String = "pending", // pending | approved | rejected
    val createdAt: Timestamp = Timestamp.now()
)
