package com.expensetracker.app.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.expensetracker.app.data.local.entity.CategoryEntity
import com.expensetracker.app.domain.model.TransactionType
import kotlinx.coroutines.flow.Flow

@Dao
interface CategoryDao {

    @Query("SELECT * FROM categories WHERE userId = :userId ORDER BY isDefault DESC, name ASC")
    fun observeAll(userId: String): Flow<List<CategoryEntity>>

    @Query("SELECT * FROM categories WHERE userId = :userId AND type = :type AND isArchived = 0 ORDER BY isDefault DESC, name ASC")
    fun observeByType(userId: String, type: TransactionType): Flow<List<CategoryEntity>>

    @Query("SELECT * FROM categories WHERE id = :id")
    suspend fun getById(id: Long): CategoryEntity?

    @Query("SELECT COUNT(*) FROM categories WHERE userId = :userId")
    suspend fun countByUserId(userId: String): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(category: CategoryEntity): Long

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertAll(categories: List<CategoryEntity>)

    @Update
    suspend fun update(category: CategoryEntity)

    @Delete
    suspend fun delete(category: CategoryEntity)

    @Query("SELECT COUNT(*) FROM transactions WHERE userId = :userId AND categoryId = :categoryId")
    suspend fun getTransactionCount(userId: String, categoryId: Long): Int
}
