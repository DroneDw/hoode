// File: TicketType.kt
package com.example.mybalaka.model

import com.google.firebase.firestore.IgnoreExtraProperties

@IgnoreExtraProperties  // ✅ ADD THIS
data class TicketType(
    val id: String = "",
    val name: String = "",
    val price: Double = 0.0,
    val quantity: Int = 0,
    val sold: Int = 0,
    val available: Int = 0
) {
    // These are computed - Firestore will ignore them automatically
    val total: Int get() = quantity
    val remaining: Int get() = quantity - sold
}