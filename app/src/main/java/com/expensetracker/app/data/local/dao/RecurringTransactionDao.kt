package com.expensetracker.app.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.expensetracker.app.data.local.entity.RecurringTransactionEntity
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

@Dao
interface RecurringTransactionDao {

    @Query("SELECT * FROM recurring_transactions WHERE userId = :userId ORDER BY id ASC")
    fun observeAll(userId: String): Flow<List<RecurringTransactionEntity>>

    @Query("SELECT * FROM recurring_transactions WHERE userId = :userId AND isActive = 1 ORDER BY nextOccurrence ASC")
    fun observeActive(userId: String): Flow<List<RecurringTransactionEntity>>

    @Query("""
        SELECT * FROM recurring_transactions
        WHERE userId = :userId AND isActive = 1 AND nextOccurrence <= :asOfDate
    """)
    suspend fun getDueRecurringTransactions(userId: String, asOfDate: LocalDate): List<RecurringTransactionEntity>

    @Query("SELECT * FROM recurring_transactions WHERE id = :id")
    suspend fun getById(id: Long): RecurringTransactionEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(recurringTransaction: RecurringTransactionEntity): Long

    @Update
    suspend fun update(recurringTransaction: RecurringTransactionEntity)

    @Delete
    suspend fun delete(recurringTransaction: RecurringTransactionEntity)

    @Query("DELETE FROM recurring_transactions WHERE id = :id")
    suspend fun deleteById(id: Long)
}
