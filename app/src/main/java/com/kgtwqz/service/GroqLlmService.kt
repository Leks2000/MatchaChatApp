package com.example.service

import com.example.data.MessageEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.IOException
import java.util.concurrent.TimeUnit

class GroqLlmService : LlmService {

    private val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()

    private val jsonMediaType = "application/json; charset=utf-8".toMediaType()

    override fun streamCompletions(
        systemPrompt: String,
        history: List<MessageEntity>,
        apiKey: String
    ): Flow<LlmChunk> = flow {
        if (apiKey.isBlank() || apiKey.contains("placeholder")) {
            emit(LlmChunk.Error("Ключ API Groq не настроен. Пожалуйста, добавьте GROQ_API_KEY в панели Secrets в AI Studio."))
            return@flow
        }

        val requestBodyString = buildRequestBody(systemPrompt, history)
        val request = Request.Builder()
            .url("https://api.api-key-safe.org/proxy/groq/openai/v1/chat/completions") // Try direct Groq URL first, we'll configure fallback to official groq.com URL
            .url("https://api.groq.com/openai/v1/chat/completions")
            .addHeader("Authorization", "Bearer $apiKey")
            .addHeader("Content-Type", "application/json")
            .post(requestBodyString.toRequestBody(jsonMediaType))
            .build()

        try {
            val response = client.newCall(request).execute()
            if (!response.isSuccessful) {
                val errorMsg = response.body?.string() ?: "Неизвестная ошибка"
                emit(LlmChunk.Error("Ошибка Сервера (код ${response.code}): $errorMsg"))
                return@flow
            }

            val responseBody = response.body
            if (responseBody == null) {
                emit(LlmChunk.Error("Упс, пустой поток ответа от Groq."))
                return@flow
            }

            val reader = responseBody.charStream().buffered()
            var line: String? = reader.readLine()
            while (line != null) {
                val trimmed = line.trim()
                if (trimmed.startsWith("data: ")) {
                    val data = trimmed.substring(6).trim()
                    if (data == "[DONE]") {
                        emit(LlmChunk.Done)
                        break
                    } else if (data.isNotEmpty()) {
                        val token = parseContentFromChunk(data)
                        if (token != null) {
                            emit(LlmChunk.Content(token))
                        }
                    }
                }
                line = reader.readLine()
            }
        } catch (e: IOException) {
            emit(LlmChunk.Error("Сбой сети: ${e.localizedMessage ?: "Проверьте интернет-соединение."}"))
        } catch (e: Exception) {
            emit(LlmChunk.Error("Произошла ошибка: ${e.localizedMessage}"))
        }
    }.flowOn(Dispatchers.IO)

    private fun buildRequestBody(systemPrompt: String, history: List<MessageEntity>): String {
        val jsonBuilder = StringBuilder()
        jsonBuilder.append("{\n")
        jsonBuilder.append("  \"model\": \"llama-3.3-70b-versatile\",\n")
        jsonBuilder.append("  \"stream\": true,\n")
        jsonBuilder.append("  \"messages\": [\n")

        // System message
        jsonBuilder.append("    {\n")
        jsonBuilder.append("      \"role\": \"system\",\n")
        jsonBuilder.append("      \"content\": ${escapeJsonString(systemPrompt)}\n")
        jsonBuilder.append("    }")

        // Conversations history
        for (msg in history) {
            jsonBuilder.append(",\n    {\n")
            jsonBuilder.append("      \"role\": ${escapeJsonString(msg.role)},\n")
            jsonBuilder.append("      \"content\": ${escapeJsonString(msg.content)}\n")
            jsonBuilder.append("    }")
        }

        jsonBuilder.append("\n  ]\n")
        jsonBuilder.append("}")
        return jsonBuilder.toString()
    }

    private fun escapeJsonString(value: String): String {
        val escaped = value
            .replace("\\", "\\\\")
            .replace("\"", "\\\"")
            .replace("\n", "\\n")
            .replace("\r", "\\r")
            .replace("\t", "\\t")
        return "\"$escaped\""
    }

    private fun parseContentFromChunk(json: String): String? {
        val key = "\"content\":\""
        val index = json.indexOf(key)
        if (index == -1) return null
        val start = index + key.length
        val sb = StringBuilder()
        var i = start
        val len = json.length
        while (i < len) {
            val c = json[i]
            if (c == '"') {
                // Ensure double-quote is not preceded by an odd number of backslashes
                var backslashes = 0
                var j = i - 1
                while (j >= start && json[j] == '\\') {
                    backslashes++
                    j--
                }
                if (backslashes % 2 == 0) {
                    break // Unescaped double quote shows end of string
                } else {
                    // It was escaped. We remove the escape character and insert quote
                    if (sb.isNotEmpty()) {
                        sb.deleteAt(sb.length - 1)
                    }
                    sb.append('"')
                }
            } else if (c == '\\' && i + 1 < len) {
                val next = json[i + 1]
                when (next) {
                    'n' -> { sb.append('\n'); i++ }
                    't' -> { sb.append('\t'); i++ }
                    'r' -> { sb.append('\r'); i++ }
                    '\\' -> { sb.append('\\'); i++ }
                    '"' -> { sb.append('"'); i++ }
                    else -> { sb.append(c) }
                }
            } else {
                sb.append(c)
            }
            i++
        }
        return sb.toString()
    }
}
