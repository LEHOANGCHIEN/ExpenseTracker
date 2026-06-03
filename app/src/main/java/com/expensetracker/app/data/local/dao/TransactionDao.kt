package com.expensetracker.app.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.expensetracker.app.data.local.entity.TransactionEntity
import com.expensetracker.app.domain.model.TransactionType
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

@Dao
interface TransactionDao {

    @Query("SELECT * FROM transactions WHERE userId = :userId AND parentSplitId IS NULL ORDER BY date DESC, createdAt DESC")
    fun observeAll(userId: String): Flow<List<TransactionEntity>>

    @Query("""
        SELECT * FROM transactions
        WHERE userId = :userId AND date BETWEEN :start AND :end AND parentSplitId IS NULL
        ORDER BY date DESC, createdAt DESC
    """)
    fun observeByDateRange(userId: String, start: LocalDate, end: LocalDate): Flow<List<TransactionEntity>>

    @Query("""
        SELECT * FROM transactions
        WHERE userId = :userId AND categoryId = :categoryId AND parentSplitId IS NULL
        ORDER BY date DESC, createdAt DESC
    """)
    fun observeByCategory(userId: String, categoryId: Long): Flow<List<TransactionEntity>>

    @Query("""
        SELECT * FROM transactions
        WHERE userId = :userId AND walletId = :walletId AND parentSplitId IS NULL
        ORDER BY date DESC, createdAt DESC
    """)
    fun observeByWallet(userId: String, walletId: Long): Flow<List<TransactionEntity>>

    @Query("SELECT * FROM transactions WHERE userId = :userId AND parentSplitId = :parentId ORDER BY id ASC")
    fun observeSplitChildren(userId: String, parentId: Long): Flow<List<TransactionEntity>>

    @Query("""
        SELECT COALESCE(SUM(amount), 0.0) FROM transactions
        WHERE userId = :userId AND type = :type AND date BETWEEN :start AND :end AND parentSplitId IS NULL
    """)
    fun getTotalByTypeAndDateRange(
        userId: String,
        type: TransactionType,
        start: LocalDate,
        end: LocalDate,
    ): Flow<Double>

    @Query("""
        SELECT COALESCE(SUM(amount), 0.0) FROM transactions
        WHERE userId = :userId AND categoryId = :categoryId AND date BETWEEN :start AND :end AND parentSplitId IS NULL
    """)
    fun getSumByCategoryAndDateRange(
        userId: String,
        categoryId: Long,
        start: LocalDate,
        end: LocalDate,
    ): Flow<Double>

    @Query("""
        SELECT * FROM transactions
        WHERE userId = :userId AND note LIKE '%' || :query || '%' AND parentSplitId IS NULL
        ORDER BY date DESC
    """)
    fun searchByNote(userId: String, query: String): Flow<List<TransactionEntity>>

    @Query("SELECT * FROM transactions WHERE id = :id")
    suspend fun getById(id: Long): TransactionEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(transaction: TransactionEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(transactions: List<TransactionEntity>)

    @Update
    suspend fun update(transaction: TransactionEntity)

    @Delete
    suspend fun delete(transaction: TransactionEntity)

    @Query("DELETE FROM transactions WHERE id = :id")
    suspend fun deleteById(id: Long)
}
