package com.expensetracker.app.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.expensetracker.app.data.local.entity.WalletEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface WalletDao {

    @Query("SELECT * FROM wallets WHERE userId = :userId ORDER BY createdAt ASC")
    fun observeAll(userId: String): Flow<List<WalletEntity>>

    @Query("SELECT * FROM wallets WHERE userId = :userId AND id = :id")
    fun observeById(userId: String, id: Long): Flow<WalletEntity?>

    @Query("SELECT * FROM wallets WHERE id = :id")
    suspend fun getById(id: Long): WalletEntity?

    @Query("""
        SELECT COALESCE(SUM(w.initialBalance), 0.0) +
               COALESCE(SUM(CASE
                   WHEN t.type = 'INCOME' THEN t.amount
                   WHEN t.type = 'EXPENSE' THEN -t.amount
                   ELSE 0.0 END), 0.0)
        FROM wallets w
        LEFT JOIN transactions t ON w.id = t.walletId AND t.parentSplitId IS NULL AND t.userId = :userId
        WHERE w.userId = :userId
    """)
    fun getTotalBalance(userId: String): Flow<Double>

    @Query("SELECT COUNT(*) FROM wallets WHERE userId = :userId")
    suspend fun countByUserId(userId: String): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(wallet: WalletEntity): Long

    @Update
    suspend fun update(wallet: WalletEntity)

    @Delete
    suspend fun delete(wallet: WalletEntity)

    @Query("DELETE FROM wallets WHERE id = :id")
    suspend fun deleteById(id: Long)
}
