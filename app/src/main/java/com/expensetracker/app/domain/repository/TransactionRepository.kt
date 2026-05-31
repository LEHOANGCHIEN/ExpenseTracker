package com.expensetracker.app.domain.repository

import com.expensetracker.app.domain.model.Transaction
import com.expensetracker.app.domain.model.TransactionType
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

interface TransactionRepository {
    fun observeAll(): Flow<List<Transaction>>
    fun observeByDateRange(start: LocalDate, end: LocalDate): Flow<List<Transaction>>
    fun observeByCategory(categoryId: Long): Flow<List<Transaction>>
    fun observeByWallet(walletId: Long): Flow<List<Transaction>>
    fun observeSplitChildren(parentId: Long): Flow<List<Transaction>>
    fun getTotalByType(type: TransactionType, start: LocalDate, end: LocalDate): Flow<Double>
    fun getSumByCategoryAndDateRange(categoryId: Long, start: LocalDate, end: LocalDate): Flow<Double>
    fun search(query: String): Flow<List<Transaction>>
    suspend fun getById(id: Long): Transaction?
    suspend fun add(transaction: Transaction): Long
    suspend fun update(transaction: Transaction)
    suspend fun delete(id: Long)
    suspend fun split(parentId: Long, children: List<Transaction>)
}
