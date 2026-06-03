package com.expensetracker.app.data.repository

import com.expensetracker.app.data.local.CurrentUserProvider
import com.expensetracker.app.data.local.dao.CategoryDao
import com.expensetracker.app.data.mapper.toDomain
import com.expensetracker.app.data.mapper.toEntity
import com.expensetracker.app.data.remote.firestore.FirestoreSyncService
import com.expensetracker.app.domain.model.Category
import com.expensetracker.app.domain.model.TransactionType
import com.expensetracker.app.domain.repository.CategoryRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CategoryRepositoryImpl @Inject constructor(
    private val categoryDao: CategoryDao,
    private val currentUserProvider: CurrentUserProvider,
    private val firestoreSyncService: FirestoreSyncService,
) : CategoryRepository {

    override fun observeAll(): Flow<List<Category>> =
        categoryDao.observeAll(currentUserProvider.uid).map { list -> list.map { it.toDomain() } }

    override fun observeByType(type: TransactionType): Flow<List<Category>> =
        categoryDao.observeByType(currentUserProvider.uid, type).map { list -> list.map { it.toDomain() } }

    override suspend fun getById(id: Long): Category? =
        categoryDao.getById(id)?.toDomain()

    override suspend fun add(category: Category): Long {
        val entity = category.toEntity().copy(userId = currentUserProvider.uid)
        val id = categoryDao.insert(entity)
        firestoreSyncService.pushCategory(entity.copy(id = id))
        return id
    }

    override suspend fun update(category: Category) {
        val entity = category.toEntity().copy(userId = currentUserProvider.uid)
        categoryDao.update(entity)
        firestoreSyncService.pushCategory(entity)
    }

    override suspend fun delete(id: Long) {
        val entity = categoryDao.getById(id) ?: return
        categoryDao.delete(entity)
        firestoreSyncService.deleteCategory(id)
    }

    override suspend fun getTransactionCount(categoryId: Long): Int =
        categoryDao.getTransactionCount(currentUserProvider.uid, categoryId)
}
