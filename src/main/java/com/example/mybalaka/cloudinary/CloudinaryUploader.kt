package com.example.mybalaka.cloudinary

import android.content.Context
import android.net.Uri
import com.cloudinary.android.MediaManager
import com.cloudinary.android.callback.ErrorInfo
import com.cloudinary.android.callback.UploadCallback
import java.io.File
import java.io.FileOutputStream

object CloudinaryUploader {

    fun uploadFile(
        context: Context,
        uri: Uri,
        onProgress: (Int) -> Unit,
        onSuccess: (String) -> Unit,
        onError: (String) -> Unit
    ) {
        try {
            val inputStream = context.contentResolver.openInputStream(uri)
            val file = File(context.cacheDir, "upload_${System.currentTimeMillis()}")
            val outputStream = FileOutputStream(file)

            inputStream?.copyTo(outputStream)
            inputStream?.close()
            outputStream.close()

            MediaManager.get()
                .upload(file.absolutePath)
                .unsigned("mybalaka_upload")
                .option("resource_type", "auto")
                .callback(object : UploadCallback {

                    override fun onStart(requestId: String?) {
                        onProgress(0)
                    }

                    override fun onProgress(
                        requestId: String?,
                        bytes: Long,
                        totalBytes: Long
                    ) {
                        if (totalBytes > 0) {
                            val progress = (bytes * 100 / totalBytes).toInt()
                            onProgress(progress)
                        }
                    }

                    override fun onSuccess(
                        requestId: String?,
                        resultData: Map<*, *>
                    ) {
                        val url = resultData["secure_url"] as? String ?: ""
                        file.delete()
                        onSuccess(url)
                    }

                    override fun onError(
                        requestId: String?,
                        error: ErrorInfo?
                    ) {
                        file.delete()
                        onError(error?.description ?: "Upload failed")
                    }

                    override fun onReschedule(
                        requestId: String?,
                        error: ErrorInfo?
                    ) {
                        file.delete()
                        onError("Upload rescheduled")
                    }
                })
                .dispatch()

        } catch (e: Exception) {
            onError(e.message ?: "File processing failed")
        }
    }
}
