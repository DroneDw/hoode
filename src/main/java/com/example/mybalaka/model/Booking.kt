package com.example.mybalaka.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId

data class Booking(
    @DocumentId
    var id: String = "",

    var serviceId: String = "",
    var customerId: String = "",
    var providerId: String = "",

    var status: String = "pending",  // pending, accepted, rejected, completed

    var createdAt: Timestamp? = null,
    var updatedAt: Timestamp? = null,

    // Added for easier display in ProviderHomeScreen
    var serviceTitle: String = "",
    var userName: String = ""
)
