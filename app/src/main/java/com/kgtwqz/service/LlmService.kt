package com.example.service

import com.example.data.MessageEntity
import kotlinx.coroutines.flow.Flow

interface LlmService {
    fun streamCompletions(
        systemPrompt: String,
        history: List<MessageEntity>,
        apiKey: String
    ): Flow<LlmChunk>
}

sealed class LlmChunk {
    data class Content(val text: String) : LlmChunk()
    data class Error(val message: String) : LlmChunk()
    object Done : LlmChunk()
}
