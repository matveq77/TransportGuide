package com.example.transportguide.utils

import okhttp3.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.asRequestBody
import org.json.JSONObject
import java.io.File
import java.io.IOException

object ImageUploader {
    private val client = OkHttpClient()

    // Вставлен ваш ключ
    private const val PUBLIC_KEY = "public_t5H5Zij3Zs9La7BOXyoNpYYqiIE="
    private const val UPLOAD_ENDPOINT = "https://upload.imagekit.io/api/v1/files/upload"

    fun uploadImage(file: File, onResult: (String?) -> Unit) {
        val requestBody = MultipartBody.Builder()
            .setType(MultipartBody.FORM)
            .addFormDataPart("file", file.name, file.asRequestBody("image/*".toMediaTypeOrNull()))
            .addFormDataPart("fileName", "img_${System.currentTimeMillis()}.jpg")
            .addFormDataPart("publicKey", PUBLIC_KEY)
            .build()

        val request = Request.Builder()
            .url(UPLOAD_ENDPOINT)
            .post(requestBody)
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                onResult(null)
            }

            override fun onResponse(call: Call, response: Response) {
                try {
                    val body = response.body?.string()
                    if (response.isSuccessful && body != null) {
                        val url = JSONObject(body).getString("url")
                        onResult(url)
                    } else {
                        onResult(null)
                    }
                } catch (e: Exception) {
                    onResult(null)
                }
            }
        })
    }
}