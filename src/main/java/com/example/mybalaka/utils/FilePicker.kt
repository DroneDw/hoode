package com.example.mybalaka.utils

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import androidx.activity.result.contract.ActivityResultContract
import androidx.activity.result.contract.ActivityResultContracts

object FilePicker {

    /** Allowed MIME types for Cloudinary uploads */
    val mimeTypes = arrayOf(
        "image/*",
        "application/pdf",
        "application/msword",
        "application/vnd.openxmlformats-officedocument.wordprocessingml.document"
    )

    /** Max file size: 10MB (adjust if needed) */
    private const val MAX_FILE_SIZE_MB = 10

    /**
     * ✅ FIX: Explicit cast to nullable contract type
     */
    fun getImagePickerContract(): ActivityResultContract<String?, Uri?> {
        return ActivityResultContracts.GetContent() as ActivityResultContract<String?, Uri?>
    }

    fun getFileName(context: Context, uri: Uri): String {
        val cursor = context.contentResolver.query(uri, null, null, null, null)
        cursor?.use {
            val nameIndex = it.getColumnIndex(OpenableColumns.DISPLAY_NAME)
            it.moveToFirst()
            return it.getString(nameIndex)
        }
        return "file"
    }

    fun getFileSizeMB(context: Context, uri: Uri): Long {
        val cursor = context.contentResolver.query(uri, null, null, null, null)
        cursor?.use {
            val sizeIndex = it.getColumnIndex(OpenableColumns.SIZE)
            it.moveToFirst()
            val sizeInBytes = it.getLong(sizeIndex)
            return sizeInBytes / (1024 * 1024)
        }
        return 0
    }

    fun isFileSizeAllowed(context: Context, uri: Uri): Boolean {
        return getFileSizeMB(context, uri) <= MAX_FILE_SIZE_MB
    }

    fun getFileType(context: Context, uri: Uri): FileType {
        val type = context.contentResolver.getType(uri) ?: return FileType.UNKNOWN
        return when {
            type.startsWith("image") -> FileType.IMAGE
            type == "application/pdf" -> FileType.PDF
            type.contains("word") -> FileType.DOCUMENT
            else -> FileType.UNKNOWN
        }
    }

    enum class FileType {
        IMAGE,
        PDF,
        DOCUMENT,
        UNKNOWN
    }
}