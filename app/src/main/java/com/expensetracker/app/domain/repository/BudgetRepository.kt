package com.expensetracker.app.domain.repository

import com.expensetracker.app.domain.model.Budget
import kotlinx.coroutines.flow.Flow

interface BudgetRepository {
    fun observeAll(): Flow<List<Budget>>
    fun observeActive(): Flow<List<Budget>>
    fun observeByCategory(categoryId: Long): Flow<List<Budget>>
    suspend fun getById(id: Long): Budget?
    suspend fun add(budget: Budget): Long
    suspend fun update(budget: Budget)
    suspend fun delete(id: Long)
}
