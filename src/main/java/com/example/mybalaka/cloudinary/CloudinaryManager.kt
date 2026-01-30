package com.example.mybalaka.cloudinary

import com.cloudinary.android.MediaManager

object CloudinaryManager {

    fun init(context: android.content.Context) {
        val config = mapOf(
            "cloud_name" to "drf2492m7",
            "secure" to true
        )

        MediaManager.init(context, config)
    }
}
