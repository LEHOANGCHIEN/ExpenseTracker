package com.expensetracker.app.data.repository

import com.expensetracker.app.data.local.dao.BudgetDao
import com.expensetracker.app.data.mapper.toDomain
import com.expensetracker.app.data.mapper.toEntity
import com.expensetracker.app.domain.model.Budget
import com.expensetracker.app.domain.repository.BudgetRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BudgetRepositoryImpl @Inject constructor(
    private val budgetDao: BudgetDao,
) : BudgetRepository {

    override fun observeAll(): Flow<List<Budget>> =
        budgetDao.observeAll().map { list -> list.map { it.toDomain() } }

    override fun observeActive(): Flow<List<Budget>> =
        budgetDao.observeActive().map { list -> list.map { it.toDomain() } }

    override fun observeByCategory(categoryId: Long): Flow<List<Budget>> =
        budgetDao.observeByCategory(categoryId).map { list -> list.map { it.toDomain() } }

    override suspend fun getById(id: Long): Budget? =
        budgetDao.getById(id)?.toDomain()

    override suspend fun add(budget: Budget): Long =
        budgetDao.insert(budget.toEntity())

    override suspend fun update(budget: Budget) =
        budgetDao.update(budget.toEntity())

    override suspend fun delete(id: Long) =
        budgetDao.deleteById(id)
}
