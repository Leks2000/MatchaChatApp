package com.example.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface MessageDao {
    @Query("SELECT * FROM chat_messages WHERE chatMode = :mode ORDER BY timestamp ASC")
    fun getMessagesByModeFlow(mode: String): kotlinx.coroutines.flow.Flow<List<MessageEntity>>

    @Query("SELECT * FROM chat_messages WHERE chatMode = :mode ORDER BY timestamp DESC LIMIT :limit")
    suspend fun getRecentMessages(mode: String, limit: Int): List<MessageEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMessage(message: MessageEntity)

    @Query("DELETE FROM chat_messages WHERE chatMode = :mode")
    suspend fun clearHistoryByMode(mode: String)
}
