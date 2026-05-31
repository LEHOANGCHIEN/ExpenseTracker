package com.expensetracker.app.domain.repository

import com.expensetracker.app.domain.model.Wallet
import kotlinx.coroutines.flow.Flow

interface WalletRepository {
    fun observeAll(): Flow<List<Wallet>>
    fun observeById(id: Long): Flow<Wallet?>
    fun getTotalBalance(): Flow<Double>
    suspend fun getById(id: Long): Wallet?
    suspend fun add(wallet: Wallet): Long
    suspend fun update(wallet: Wallet)
    suspend fun delete(id: Long)
}
