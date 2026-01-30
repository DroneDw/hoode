package com.example.mybalaka.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.IgnoreExtraProperties

@IgnoreExtraProperties
data class Service(
    val id: String = "",
    val title: String = "",
    val description: String = "",
    val category: String = "",
    val phone: String = "",
    val whatsapp: String = "",
    var rating: Float = 0f,
    var voteCount: Int = 0,
    val price: String = "",
    val posterUrl: String = "",
    val providerId: String = "",
    val createdAt: Timestamp = Timestamp.now(),
    val isAvailable: Boolean = true,
    val bookedBy: String? = null
) {
    companion object {
        val POPULAR_CATEGORIES = listOf(
            "Mechanic",
            "Electrician",
            "Plumber",
            "Carpenter",
            "Painter"
        )

        val ALL_CATEGORIES = listOf(
            "Mechanic",
            "Electrician",
            "Plumber",
            "Carpenter",
            "Painter",
            "Tailor",
            "Salon",
            "Gardener",
            "Dish Installer",
            "Mason",
            "Welder",
            "Cleaner",
            "Other"
        )
    }

}