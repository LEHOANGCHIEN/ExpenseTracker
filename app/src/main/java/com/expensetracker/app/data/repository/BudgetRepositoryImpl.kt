package com.expensetracker.app.data.repository

import com.expensetracker.app.data.local.CurrentUserProvider
import com.expensetracker.app.data.local.dao.BudgetDao
import com.expensetracker.app.data.mapper.toDomain
import com.expensetracker.app.data.mapper.toEntity
import com.expensetracker.app.data.remote.firestore.FirestoreSyncService
import com.expensetracker.app.domain.model.Budget
import com.expensetracker.app.domain.repository.BudgetRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BudgetRepositoryImpl @Inject constructor(
    private val budgetDao: BudgetDao,
    private val currentUserProvider: CurrentUserProvider,
    private val firestoreSyncService: FirestoreSyncService,
) : BudgetRepository {

    override fun observeAll(): Flow<List<Budget>> =
        budgetDao.observeAll(currentUserProvider.uid).map { list -> list.map { it.toDomain() } }

    override fun observeActive(): Flow<List<Budget>> =
        budgetDao.observeActive(currentUserProvider.uid).map { list -> list.map { it.toDomain() } }

    override fun observeByCategory(categoryId: Long): Flow<List<Budget>> =
        budgetDao.observeByCategory(currentUserProvider.uid, categoryId).map { list -> list.map { it.toDomain() } }

    override suspend fun getById(id: Long): Budget? =
        budgetDao.getById(id)?.toDomain()

    override suspend fun add(budget: Budget): Long {
        val entity = budget.toEntity().copy(userId = currentUserProvider.uid)
        val id = budgetDao.insert(entity)
        firestoreSyncService.pushBudget(entity.copy(id = id))
        return id
    }

    override suspend fun update(budget: Budget) {
        val entity = budget.toEntity().copy(userId = currentUserProvider.uid)
        budgetDao.update(entity)
        firestoreSyncService.pushBudget(entity)
    }

    override suspend fun delete(id: Long) {
        budgetDao.deleteById(id)
        firestoreSyncService.deleteBudget(id)
    }
}
