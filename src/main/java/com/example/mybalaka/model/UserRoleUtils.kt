package com.example.mybalaka.model

fun String.toUserRole(): UserRole {
    return when (this.lowercase()) {
        "user" -> UserRole.USER
        "seller" -> UserRole.SELLER
        "provider" -> UserRole.PROVIDER
        "organizer", "organiser" -> UserRole.ORGANIZER
        "admin" -> UserRole.ADMIN
        else -> UserRole.USER
    }
}
