package com.example.mybalaka.utils

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.core.content.FileProvider
import com.example.mybalaka.model.Announcement
import java.io.File
import java.io.FileOutputStream
import java.net.URL
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

suspend fun shareAnnouncement(context: Context, announcement: Announcement) {
    val shareText = buildString {
        appendLine(announcement.title)
        appendLine()
        appendLine(announcement.description)
        appendLine()
        append("📢 Posted via MyBalaka App")
    }

    val intent = Intent(Intent.ACTION_SEND)

    if (announcement.imageUrls.isNotEmpty()) {
        val uri = downloadImageToCache(context, announcement.imageUrls.first())
        if (uri != null) {
            intent.type = "image/*"
            intent.putExtra(Intent.EXTRA_STREAM, uri)
            intent.putExtra(Intent.EXTRA_TEXT, shareText)
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        } else {
            // fallback to text only if image download fails
            intent.type = "text/plain"
            intent.putExtra(Intent.EXTRA_TEXT, shareText)
        }
    } else {
        intent.type = "text/plain"
        intent.putExtra(Intent.EXTRA_TEXT, shareText)
    }

    context.startActivity(Intent.createChooser(intent, "Share announcement"))
}

private suspend fun downloadImageToCache(context: Context, imageUrl: String): Uri? {
    return withContext(Dispatchers.IO) {
        try {
            val url = URL(imageUrl)
            val connection = url.openConnection()
            connection.connect()
            val input = connection.getInputStream()
            val file = File(context.cacheDir, "shared_image.png")
            val output = FileOutputStream(file)
            input.copyTo(output)
            output.close()
            input.close()
            FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}
