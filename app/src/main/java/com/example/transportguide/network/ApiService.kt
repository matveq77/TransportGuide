package com.example.transportguide.network

import retrofit2.http.GET
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

// Модель данных от сервера
data class NewsResponse(
    val id: Int,
    val title: String,
    val body: String
)

interface ApiService {
    @GET("posts") // Берем список постов
    suspend fun getTransportNews(): List<NewsResponse>

    companion object {
        private const val BASE_URL = "https://jsonplaceholder.typicode.com/"

        fun create(): ApiService {
            return Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build()
                .create(ApiService::class.java)
        }
    }
}