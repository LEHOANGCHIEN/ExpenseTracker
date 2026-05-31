package com.expensetracker.app.data.repository

import com.expensetracker.app.data.local.dao.TransactionDao
import com.expensetracker.app.data.mapper.toDomain
import com.expensetracker.app.data.mapper.toEntity
import com.expensetracker.app.domain.model.Transaction
import com.expensetracker.app.domain.model.TransactionType
import com.expensetracker.app.domain.repository.TransactionRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.LocalDate
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TransactionRepositoryImpl @Inject constructor(
    private val transactionDao: TransactionDao,
) : TransactionRepository {

    override fun observeAll(): Flow<List<Transaction>> =
        transactionDao.observeAll().map { list -> list.map { it.toDomain() } }

    override fun observeByDateRange(start: LocalDate, end: LocalDate): Flow<List<Transaction>> =
        transactionDao.observeByDateRange(start, end).map { list -> list.map { it.toDomain() } }

    override fun observeByCategory(categoryId: Long): Flow<List<Transaction>> =
        transactionDao.observeByCategory(categoryId).map { list -> list.map { it.toDomain() } }

    override fun observeByWallet(walletId: Long): Flow<List<Transaction>> =
        transactionDao.observeByWallet(walletId).map { list -> list.map { it.toDomain() } }

    override fun observeSplitChildren(parentId: Long): Flow<List<Transaction>> =
        transactionDao.observeSplitChildren(parentId).map { list -> list.map { it.toDomain() } }

    override fun getTotalByType(type: TransactionType, start: LocalDate, end: LocalDate): Flow<Double> =
        transactionDao.getTotalByTypeAndDateRange(type, start, end)

    override fun getSumByCategoryAndDateRange(categoryId: Long, start: LocalDate, end: LocalDate): Flow<Double> =
        transactionDao.getSumByCategoryAndDateRange(categoryId, start, end)

    override fun search(query: String): Flow<List<Transaction>> =
        transactionDao.searchByNote(query).map { list -> list.map { it.toDomain() } }

    override suspend fun getById(id: Long): Transaction? =
        transactionDao.getById(id)?.toDomain()

    override suspend fun add(transaction: Transaction): Long =
        transactionDao.insert(transaction.toEntity())

    override suspend fun update(transaction: Transaction) =
        transactionDao.update(transaction.toEntity())

    override suspend fun delete(id: Long) =
        transactionDao.deleteById(id)

    override suspend fun split(parentId: Long, children: List<Transaction>) {
        transactionDao.insertAll(children.map { it.copy(parentSplitId = parentId).toEntity() })
    }
}
