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

    @Query("SELECT * FROM wallets ORDER BY createdAt ASC")
    fun observeAll(): Flow<List<WalletEntity>>

    @Query("SELECT * FROM wallets WHERE id = :id")
    fun observeById(id: Long): Flow<WalletEntity?>

    @Query("SELECT * FROM wallets WHERE id = :id")
    suspend fun getById(id: Long): WalletEntity?

    /**
     * Total balance = sum of all initialBalances + net of all transactions.
     * Transfer transactions are excluded from this calculation.
     */
    @Query("""
        SELECT COALESCE(SUM(w.initialBalance), 0.0) +
               COALESCE(SUM(CASE
                   WHEN t.type = 'INCOME' THEN t.amount
                   WHEN t.type = 'EXPENSE' THEN -t.amount
                   ELSE 0.0 END), 0.0)
        FROM wallets w
        LEFT JOIN transactions t ON w.id = t.walletId AND t.parentSplitId IS NULL
    """)
    fun getTotalBalance(): Flow<Double>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(wallet: WalletEntity): Long

    @Update
    suspend fun update(wallet: WalletEntity)

    @Delete
    suspend fun delete(wallet: WalletEntity)

    @Query("DELETE FROM wallets WHERE id = :id")
    suspend fun deleteById(id: Long)
}
