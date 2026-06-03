package com.expensetracker.app.data.repository

import com.expensetracker.app.data.local.CurrentUserProvider
import com.expensetracker.app.data.local.dao.WalletDao
import com.expensetracker.app.data.mapper.toDomain
import com.expensetracker.app.data.mapper.toEntity
import com.expensetracker.app.data.remote.firestore.FirestoreSyncService
import com.expensetracker.app.domain.model.Wallet
import com.expensetracker.app.domain.repository.WalletRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class WalletRepositoryImpl @Inject constructor(
    private val walletDao: WalletDao,
    private val currentUserProvider: CurrentUserProvider,
    private val firestoreSyncService: FirestoreSyncService,
) : WalletRepository {

    override fun observeAll(): Flow<List<Wallet>> =
        walletDao.observeAll(currentUserProvider.uid).map { list -> list.map { it.toDomain() } }

    override fun observeById(id: Long): Flow<Wallet?> =
        walletDao.observeById(currentUserProvider.uid, id).map { it?.toDomain() }

    override fun getTotalBalance(): Flow<Double> =
        walletDao.getTotalBalance(currentUserProvider.uid)

    override suspend fun getById(id: Long): Wallet? =
        walletDao.getById(id)?.toDomain()

    override suspend fun add(wallet: Wallet): Long {
        val entity = wallet.toEntity().copy(userId = currentUserProvider.uid)
        val id = walletDao.insert(entity)
        firestoreSyncService.pushWallet(entity.copy(id = id))
        return id
    }

    override suspend fun update(wallet: Wallet) {
        val entity = wallet.toEntity().copy(userId = currentUserProvider.uid)
        walletDao.update(entity)
        firestoreSyncService.pushWallet(entity)
    }

    override suspend fun delete(id: Long) {
        walletDao.deleteById(id)
        firestoreSyncService.deleteWallet(id)
    }
}
