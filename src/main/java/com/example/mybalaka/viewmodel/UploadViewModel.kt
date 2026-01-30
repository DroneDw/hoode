package com.example.mybalaka.viewmodel

import android.content.Context
import android.net.Uri
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.example.mybalaka.cloudinary.CloudinaryUploader

class UploadViewModel : ViewModel() {

    var uploadProgress by mutableStateOf(0)
        private set

    var uploadedUrl by mutableStateOf<String?>(null)
        private set

    var isUploading by mutableStateOf(false)
        private set

    fun upload(context: Context, uri: Uri) {
        isUploading = true

        CloudinaryUploader.uploadFile(
            context = context,
            uri = uri,
            onProgress = { uploadProgress = it },
            onSuccess = {
                uploadedUrl = it
                isUploading = false
            },
            onError = {
                isUploading = false
                uploadProgress = 0 // Reset progress on error
                uploadedUrl = null
            }
        )
    }
}