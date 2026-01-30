package com.example.mybalaka.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.IgnoreExtraProperties

@IgnoreExtraProperties
data class Announcement(
    @DocumentId
    val id: String = "",

    val title: String = "",
    val description: String = "",
    val imageUrls: List<String> = emptyList(),
    val createdAt: Timestamp = Timestamp.now(),
    val createdBy: String = "",

    val expiresAt: Timestamp? = null, // ✅ Expiry date
    val readBy: List<String> = emptyList(),

    // ✅ New fields for download functionality
    val downloadableFileUrl: String? = null, // URL of file to download
    val showDownloadButton: Boolean = false  // Should the download button be displayed
)
