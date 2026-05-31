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

    @Query("SELECT * FROM transactions WHERE parentSplitId IS NULL ORDER BY date DESC, createdAt DESC")
    fun observeAll(): Flow<List<TransactionEntity>>

    @Query("""
        SELECT * FROM transactions
        WHERE date BETWEEN :start AND :end AND parentSplitId IS NULL
        ORDER BY date DESC, createdAt DESC
    """)
    fun observeByDateRange(start: LocalDate, end: LocalDate): Flow<List<TransactionEntity>>

    @Query("""
        SELECT * FROM transactions
        WHERE categoryId = :categoryId AND parentSplitId IS NULL
        ORDER BY date DESC, createdAt DESC
    """)
    fun observeByCategory(categoryId: Long): Flow<List<TransactionEntity>>

    @Query("""
        SELECT * FROM transactions
        WHERE walletId = :walletId AND parentSplitId IS NULL
        ORDER BY date DESC, createdAt DESC
    """)
    fun observeByWallet(walletId: Long): Flow<List<TransactionEntity>>

    @Query("SELECT * FROM transactions WHERE parentSplitId = :parentId ORDER BY id ASC")
    fun observeSplitChildren(parentId: Long): Flow<List<TransactionEntity>>

    @Query("""
        SELECT COALESCE(SUM(amount), 0.0) FROM transactions
        WHERE type = :type AND date BETWEEN :start AND :end AND parentSplitId IS NULL
    """)
    fun getTotalByTypeAndDateRange(
        type: TransactionType,
        start: LocalDate,
        end: LocalDate,
    ): Flow<Double>

    @Query("""
        SELECT COALESCE(SUM(amount), 0.0) FROM transactions
        WHERE categoryId = :categoryId AND date BETWEEN :start AND :end AND parentSplitId IS NULL
    """)
    fun getSumByCategoryAndDateRange(
        categoryId: Long,
        start: LocalDate,
        end: LocalDate,
    ): Flow<Double>

    @Query("""
        SELECT * FROM transactions
        WHERE note LIKE '%' || :query || '%' AND parentSplitId IS NULL
        ORDER BY date DESC
    """)
    fun searchByNote(query: String): Flow<List<TransactionEntity>>

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
