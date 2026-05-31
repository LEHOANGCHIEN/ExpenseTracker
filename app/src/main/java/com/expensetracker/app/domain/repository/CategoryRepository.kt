package com.expensetracker.app.domain.repository

import com.expensetracker.app.domain.model.Category
import com.expensetracker.app.domain.model.TransactionType
import kotlinx.coroutines.flow.Flow

interface CategoryRepository {
    fun observeAll(): Flow<List<Category>>
    fun observeByType(type: TransactionType): Flow<List<Category>>
    suspend fun getById(id: Long): Category?
    suspend fun add(category: Category): Long
    suspend fun update(category: Category)
    suspend fun delete(id: Long)
    suspend fun getTransactionCount(categoryId: Long): Int
}
