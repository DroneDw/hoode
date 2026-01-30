package com.example.mybalaka.data

import retrofit2.http.GET
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.await

data class WeatherDTO(
    val current: Current
) {
    data class Current(
        val temperature: Double,
        val weather_code: Int
    )
}

interface WeatherApi {
    @GET("v1/forecast?latitude=-14.9&longitude=34.8&current=temperature,weather_code&timezone=Africa/Maputo")
    suspend fun getBalakaNow(): WeatherDTO
}

object WeatherService {
    private val retrofit = Retrofit.Builder()
        .baseUrl("https://api.open-meteo.com/")
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    val api: WeatherApi = retrofit.create(WeatherApi::class.java)

    fun iconUrl(code: Int) = "https://open-meteo.com/images/weather/$code.png"
}