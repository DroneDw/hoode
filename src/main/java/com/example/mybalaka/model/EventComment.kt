package com.example.mybalaka.model

import com.google.firebase.Timestamp

data class EventComment(
    val authorId: String = "",
    val authorName: String = "",
    val text: String = "",
    val timestamp: Timestamp = Timestamp.now()
)