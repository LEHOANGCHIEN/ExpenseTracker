package com.expensetracker.app.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.expensetracker.app.data.local.entity.AiInsightEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface AiInsightDao {

    @Query("SELECT * FROM ai_insights WHERE userId = :userId AND dismissed = 0 ORDER BY generatedAt DESC")
    fun observeUndismissed(userId: String): Flow<List<AiInsightEntity>>

    @Query("SELECT * FROM ai_insights WHERE userId = :userId ORDER BY generatedAt DESC")
    fun observeAll(userId: String): Flow<List<AiInsightEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(insight: AiInsightEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(insights: List<AiInsightEntity>)

    @Query("UPDATE ai_insights SET dismissed = 1 WHERE id = :id")
    suspend fun dismiss(id: Long)

    @Query("DELETE FROM ai_insights")
    suspend fun deleteAll()
}
