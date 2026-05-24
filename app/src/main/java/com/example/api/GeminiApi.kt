package com.example.api

import com.example.BuildConfig
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Query
import java.util.concurrent.TimeUnit

@JsonClass(generateAdapter = true)
data class GenerateContentRequest(
    @Json(name = "contents") val contents: List<Content>,
    @Json(name = "systemInstruction") val systemInstruction: Content? = null
)

@JsonClass(generateAdapter = true)
data class Content(
    @Json(name = "parts") val parts: List<Part>
)

@JsonClass(generateAdapter = true)
data class Part(
    @Json(name = "text") val text: String
)

@JsonClass(generateAdapter = true)
data class GenerateContentResponse(
    @Json(name = "candidates") val candidates: List<Candidate>?
)

@JsonClass(generateAdapter = true)
data class Candidate(
    @Json(name = "content") val content: Content?
)

interface GeminiApiService {
    @POST("v1beta/models/gemini-3.5-flash:generateContent")
    suspend fun generateContent(
        @Query("key") apiKey: String,
        @Body request: GenerateContentRequest
    ): GenerateContentResponse
}

object RetrofitClient {
    private const val BASE_URL = "https://generativelanguage.googleapis.com/"

    private val moshi = Moshi.Builder()
        .addLast(KotlinJsonAdapterFactory())
        .build()

    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .build()

    private val retrofit = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .client(okHttpClient)
        .addConverterFactory(MoshiConverterFactory.create(moshi))
        .build()

    val service: GeminiApiService = retrofit.create(GeminiApiService::class.java)
}

object GeminiRepository {
    private const val SYSTEM_PROMPT = 
        "Вы — Матча Мастер, умиротворяющий и мудрый велнес-коуч и чайный сомелье. " +
        "Вы помогаете пользователю обрести гармонию, расслабиться, рассказываете о приготовлении чая матча, " +
        "дыхательных упражнениях, управлении стрессом и осознанности. " +
        "Отвечайте в мягком, поддерживающем и благородном тоне исключительно на русском языке. " +
        "Ваш ответ должен быть лаконичным (не более 3-4 предложений), мудрым и вдохновляющим. " +
        "Используйте подходящие смайлики: 🍵, 🧘, 🍃, 🌸, ✨."

    suspend fun generateResponse(userPrompt: String, history: List<Content> = emptyList()): String {
        val apiKey = BuildConfig.GEMINI_API_KEY
        if (apiKey.isBlank() || apiKey == "MY_GEMINI_API_KEY") {
            return "Привет! Настройте, пожалуйста, секретный ключ GEMINI_API_KEY в панели Secrets, чтобы я мог ответить вам мудростью веков! 🍵"
        }

        // Combine history and current prompt
        val currentContent = Content(parts = listOf(Part(text = userPrompt)))
        val fullContents = history + listOf(currentContent)

        val request = GenerateContentRequest(
            contents = fullContents,
            systemInstruction = Content(parts = listOf(Part(text = SYSTEM_PROMPT)))
        )

        return try {
            val response = RetrofitClient.service.generateContent(apiKey, request)
            response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text 
                ?: "Я задумался в молчании чая... Пожалуйста, попробуй задать свой вопрос ещё раз. 🍵"
        } catch (e: Exception) {
            "Произошла ошибка при единении с сервером: ${e.localizedMessage}. Проверьте соединение или валидность ключа! 🍃"
        }
    }
}
