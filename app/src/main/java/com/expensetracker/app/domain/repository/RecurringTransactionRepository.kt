package com.expensetracker.app.domain.repository

import com.expensetracker.app.domain.model.RecurringTransaction
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

interface RecurringTransactionRepository {
    fun observeAll(): Flow<List<RecurringTransaction>>
    fun observeActive(): Flow<List<RecurringTransaction>>
    suspend fun getDue(asOfDate: LocalDate): List<RecurringTransaction>
    suspend fun getById(id: Long): RecurringTransaction?
    suspend fun add(recurringTransaction: RecurringTransaction): Long
    suspend fun update(recurringTransaction: RecurringTransaction)
    suspend fun delete(id: Long)
}
