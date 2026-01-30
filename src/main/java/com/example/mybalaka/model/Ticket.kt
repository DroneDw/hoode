package com.example.mybalaka.model

import com.google.firebase.Timestamp

data class Ticket(
    val id: String = "",
    val eventId: String = "",
    val eventName: String = "",        // ✅ Human-readable event name
    val userId: String = "",
    val ticketTypeId: String = "",
    val ticketTypeName: String = "",   // ✅ Human-readable ticket type (VIP, Regular, etc.)
    val qrCode: String = "",
    val status: String = "active",
    val createdAt: Timestamp = Timestamp.now()
)