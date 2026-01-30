package com.example.mybalaka.network

import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONException
import org.json.JSONObject
import java.io.IOException

object TicketScanApi {

    private val client = OkHttpClient.Builder()
        .connectTimeout(10, java.util.concurrent.TimeUnit.SECONDS)
        .readTimeout(10, java.util.concurrent.TimeUnit.SECONDS)
        .build()

    fun scanTicket(
        qrCode: String,
        callback: (success: Boolean, message: String) -> Unit
    ) {
        val json = JSONObject().apply {
            put("qrCode", qrCode)
            put("scannerId", "organizer_device") // optional tracking
        }

        val request = Request.Builder()
            .url("https://paychangu-backend-g9vt.onrender.com/scan-ticket")
            .post(json.toString().toRequestBody("application/json".toMediaType()))
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                callback(false, "Network error: ${e.message}")
            }

            override fun onResponse(call: Call, response: Response) {
                try {
                    val body = response.body?.string() ?: ""
                    val res = JSONObject(body)
                    val success = res.getBoolean("success")
                    val message = res.getString("message")
                    callback(success, message)
                } catch (e: JSONException) {
                    callback(false, "Invalid server response")
                } catch (e: Exception) {
                    callback(false, "Error: ${e.message}")
                }
            }
        })
    }
}