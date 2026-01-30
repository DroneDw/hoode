package com.example.mybalaka.model
import com.google.firebase.Timestamp
data class Payment(
    val id: String = "",
    val userId: String = "",
    val eventId: String = "",
    val ticketTypeId: String = "",
    val quantity: Int = 0,
    val amount: Int = 0,
    val currency: String = "MWK",
    val status: String = "pending",
    val reference: String = "",
    val paychanguRef: String = "",
    val createdAt: Timestamp = Timestamp.now()
)