package com.expensetracker.app.data.repository

import com.expensetracker.app.data.local.dao.WalletDao
import com.expensetracker.app.data.mapper.toDomain
import com.expensetracker.app.data.mapper.toEntity
import com.expensetracker.app.domain.model.Wallet
import com.expensetracker.app.domain.repository.WalletRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class WalletRepositoryImpl @Inject constructor(
    private val walletDao: WalletDao,
) : WalletRepository {

    override fun observeAll(): Flow<List<Wallet>> =
        walletDao.observeAll().map { list -> list.map { it.toDomain() } }

    override fun observeById(id: Long): Flow<Wallet?> =
        walletDao.observeById(id).map { it?.toDomain() }

    override fun getTotalBalance(): Flow<Double> =
        walletDao.getTotalBalance()

    override suspend fun getById(id: Long): Wallet? =
        walletDao.getById(id)?.toDomain()

    override suspend fun add(wallet: Wallet): Long =
        walletDao.insert(wallet.toEntity())

    override suspend fun update(wallet: Wallet) =
        walletDao.update(wallet.toEntity())

    override suspend fun delete(id: Long) =
        walletDao.deleteById(id)
}
