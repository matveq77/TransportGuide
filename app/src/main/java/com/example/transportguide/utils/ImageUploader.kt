package com.example.transportguide.utils

import okhttp3.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.asRequestBody
import org.json.JSONObject
import java.io.File
import java.io.IOException

object ImageUploader {
    private val client = OkHttpClient()

    // ВАШИ ДАННЫЕ ИЗ CLOUDINARY
    private const val CLOUD_NAME = "dcu38obex"
    // ВСТАВЬТЕ СЮДА ИМЯ ПРЕСЕТА, КОТОРЫЙ ВЫ ВКЛЮЧИЛИ НА ШАГЕ 1
    private const val UPLOAD_PRESET = "ml_default"

    private const val UPLOAD_ENDPOINT = "https://api.cloudinary.com/v1_1/$CLOUD_NAME/image/upload"

    fun uploadImage(file: File, onResult: (String?) -> Unit) {
        val requestBody = MultipartBody.Builder()
            .setType(MultipartBody.FORM)
            .addFormDataPart("file", file.name, file.asRequestBody("image/*".toMediaTypeOrNull()))
            .addFormDataPart("upload_preset", UPLOAD_PRESET) // Обязательный параметр
            .build()

        val request = Request.Builder()
            .url(UPLOAD_ENDPOINT)
            .post(requestBody)
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                android.util.Log.e("UPLOAD_DEBUG", "Network error: ${e.message}")
                onResult(null)
            }

            override fun onResponse(call: Call, response: Response) {
                val body = response.body?.string()
                android.util.Log.d("UPLOAD_DEBUG", "Response: $body")

                if (response.isSuccessful && body != null) {
                    try {
                        // Cloudinary возвращает URL в поле "secure_url"
                        val json = JSONObject(body)
                        val url = json.getString("secure_url")
                        onResult(url)
                    } catch (e: Exception) {
                        onResult(null)
                    }
                } else {
                    onResult(null)
                }
            }
        })
    }
}