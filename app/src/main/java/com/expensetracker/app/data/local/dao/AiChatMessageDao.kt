package com.expensetracker.app.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.expensetracker.app.data.local.entity.AiChatMessageEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface AiChatMessageDao {

    @Query("SELECT * FROM ai_chat_messages WHERE sessionId = :sessionId ORDER BY timestamp ASC")
    fun observeBySession(sessionId: String): Flow<List<AiChatMessageEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(message: AiChatMessageEntity): Long

    @Query("DELETE FROM ai_chat_messages WHERE sessionId = :sessionId")
    suspend fun deleteSession(sessionId: String)

    @Query("SELECT sessionId FROM ai_chat_messages ORDER BY timestamp DESC LIMIT 1")
    suspend fun getLatestSessionId(): String?

    @Query("DELETE FROM ai_chat_messages")
    suspend fun deleteAll()
}
