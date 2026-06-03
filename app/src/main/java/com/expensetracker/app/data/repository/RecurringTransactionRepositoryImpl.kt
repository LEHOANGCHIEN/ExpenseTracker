package com.expensetracker.app.data.repository

import com.expensetracker.app.data.local.CurrentUserProvider
import com.expensetracker.app.data.local.dao.RecurringTransactionDao
import com.expensetracker.app.data.mapper.toDomain
import com.expensetracker.app.data.mapper.toEntity
import com.expensetracker.app.data.remote.firestore.FirestoreSyncService
import com.expensetracker.app.domain.model.RecurringTransaction
import com.expensetracker.app.domain.repository.RecurringTransactionRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.LocalDate
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RecurringTransactionRepositoryImpl @Inject constructor(
    private val recurringTransactionDao: RecurringTransactionDao,
    private val currentUserProvider: CurrentUserProvider,
    private val firestoreSyncService: FirestoreSyncService,
) : RecurringTransactionRepository {

    override fun observeAll(): Flow<List<RecurringTransaction>> =
        recurringTransactionDao.observeAll(currentUserProvider.uid).map { list -> list.map { it.toDomain() } }

    override fun observeActive(): Flow<List<RecurringTransaction>> =
        recurringTransactionDao.observeActive(currentUserProvider.uid).map { list -> list.map { it.toDomain() } }

    override suspend fun getDue(asOfDate: LocalDate): List<RecurringTransaction> =
        recurringTransactionDao.getDueRecurringTransactions(currentUserProvider.uid, asOfDate).map { it.toDomain() }

    override suspend fun getById(id: Long): RecurringTransaction? =
        recurringTransactionDao.getById(id)?.toDomain()

    override suspend fun add(recurringTransaction: RecurringTransaction): Long {
        val entity = recurringTransaction.toEntity().copy(userId = currentUserProvider.uid)
        val id = recurringTransactionDao.insert(entity)
        firestoreSyncService.pushRecurring(entity.copy(id = id))
        return id
    }

    override suspend fun update(recurringTransaction: RecurringTransaction) {
        val entity = recurringTransaction.toEntity().copy(userId = currentUserProvider.uid)
        recurringTransactionDao.update(entity)
        firestoreSyncService.pushRecurring(entity)
    }

    override suspend fun delete(id: Long) {
        recurringTransactionDao.deleteById(id)
        firestoreSyncService.deleteRecurring(id)
    }
}
