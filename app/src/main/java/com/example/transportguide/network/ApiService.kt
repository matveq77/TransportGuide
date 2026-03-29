package com.example.transportguide.network

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query

// Модели для Швейцарского транспорта
data class StationResponse(val stations: List<Station>)
data class Station(
    val id: String?,
    val name: String?,
    val type: String?
)

interface ApiService {
    @GET("v1/locations")
    suspend fun getStations(@Query("query") city: String = "Bern"): StationResponse

    companion object {
        private const val BASE_URL = "https://transport.opendata.ch/"

        fun create(): ApiService {
            return Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build()
                .create(ApiService::class.java)
        }
    }
}