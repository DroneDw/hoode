package com.example.mybalaka.utils

import com.google.firebase.Timestamp
import java.util.concurrent.TimeUnit

fun expiryBadge(expiresAt: Timestamp?): String? {
    if (expiresAt == null) return null

    val now = System.currentTimeMillis()
    val diff = expiresAt.toDate().time - now

    if (diff <= 0) return "Expired"

    val days = TimeUnit.MILLISECONDS.toDays(diff)
    val hours = TimeUnit.MILLISECONDS.toHours(diff) % 24
    val minutes = TimeUnit.MILLISECONDS.toMinutes(diff) % 60

    return when {
        days > 2 -> null                     // 👈 HIDE EARLY
        days >= 1 -> "Expires in $days day${if (days > 1) "s" else ""}"
        else -> "Expires in ${hours}h ${minutes}m"
    }
}

fun isExpired(expiresAt: Timestamp?): Boolean {
    return expiresAt != null && expiresAt.toDate().time < System.currentTimeMillis()
}
