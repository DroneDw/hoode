package com.example.mybalaka.payment

import retrofit2.http.*
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

data class PayRequest(
    val amount: Double,
    val phone: String,
    val network: String,
    val userId: String,
    val itemId: String
)

data class PayResponse(
    val paymentId: String,
    val checkoutUrl: String
)

interface PayChanguApi {

    @POST("pay")
    suspend fun pay(@Body request: PayRequest): PayResponse

    @GET("payment-status/{id}")
    suspend fun status(@Path("id") id: String): Map<String, Any>

    companion object {
        fun create(): PayChanguApi =
            Retrofit.Builder()
                .baseUrl("https://paychangu-backend-g9vt.onrender.com/") // ✅ FIXED: removed space
                .addConverterFactory(GsonConverterFactory.create())
                .build()
                .create(PayChanguApi::class.java)
    }
}