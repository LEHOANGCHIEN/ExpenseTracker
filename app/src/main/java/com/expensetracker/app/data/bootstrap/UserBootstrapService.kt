package com.expensetracker.app.data.bootstrap

import android.util.Log
import com.expensetracker.app.data.local.dao.CategoryDao
import com.expensetracker.app.data.local.dao.WalletDao
import com.expensetracker.app.data.local.entity.CategoryEntity
import com.expensetracker.app.data.local.entity.WalletEntity
import com.expensetracker.app.data.remote.firestore.FirestoreCloudSyncService
import com.expensetracker.app.domain.model.TransactionType
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.time.LocalDateTime
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserBootstrapService @Inject constructor(
    private val categoryDao: CategoryDao,
    private val walletDao: WalletDao,
    private val firestoreCloudSyncService: FirestoreCloudSyncService,
) {
    // Prevents concurrent bootstrap runs (e.g. sign-in path + MainActivity LaunchedEffect
    // both firing when auth state changes — the second call short-circuits via count check).
    private val mutex = Mutex()

    suspend fun bootstrapIfNeeded(userId: String) = mutex.withLock {
        Log.d(TAG, "bootstrapIfNeeded START userId=$userId")
        if (userId.isEmpty()) {
            Log.w(TAG, "bootstrapIfNeeded ABORT: userId is empty")
            return@withLock
        }

        // Fast path: local DB already has data (normal app restart on same device).
        val localCount = categoryDao.countByUserId(userId)
        if (localCount > 0) {
            Log.d(TAG, "bootstrapIfNeeded SKIP: local already has $localCount categories")
            return@withLock
        }

        // Local is empty — pull from cloud first so an existing account on a new device
        // gets its real data rather than freshly-seeded defaults.
        firestoreCloudSyncService.syncFromCloud(userId)

        // After pull, check again.
        val countAfterPull = categoryDao.countByUserId(userId)
        if (countAfterPull > 0) {
            Log.d(TAG, "bootstrapIfNeeded DONE: restored $countAfterPull categories from cloud (existing account)")
            return@withLock
        }

        // Cloud had no data — genuinely new account. Seed defaults and push them to Firestore
        // so a future device can pull them instead of re-seeding with potentially different IDs.
        Log.d(TAG, "bootstrapIfNeeded SEEDING defaults (new account)")
        val seededWallet = seedDefaultWallet(userId)
        val seededCategories = seedDefaultCategories(userId)
        firestoreCloudSyncService.pushSeedData(userId, listOf(seededWallet), seededCategories)
        Log.d(TAG, "bootstrapIfNeeded DONE: seeded defaults and enqueued Firestore push")
    }

    private suspend fun seedDefaultWallet(userId: String): WalletEntity {
        val template = WalletEntity(
            userId = userId,
            name = "Cash",
            icon = "💵",
            color = "#26A69A",
            initialBalance = 0.0,
            currency = "VND",
            createdAt = LocalDateTime.now(),
        )
        val id = walletDao.insert(template)
        return template.copy(id = id)
    }

    private suspend fun seedDefaultCategories(userId: String): List<CategoryEntity> {
        val templates = defaultCategories(userId)
        return templates.map { cat ->
            val id = categoryDao.insert(cat)
            cat.copy(id = id)
        }
    }

    private fun defaultCategories(userId: String): List<CategoryEntity> {
        val expense = TransactionType.EXPENSE
        val income = TransactionType.INCOME
        return listOf(
            CategoryEntity(userId = userId, name = "Ăn uống",       icon = "🍔", color = "#FFA726", type = expense, isDefault = true, isArchived = false),
            CategoryEntity(userId = userId, name = "Di chuyển",     icon = "🚗", color = "#42A5F5", type = expense, isDefault = true, isArchived = false),
            CategoryEntity(userId = userId, name = "Mua sắm",       icon = "🛍️", color = "#EC407A", type = expense, isDefault = true, isArchived = false),
            CategoryEntity(userId = userId, name = "Giải trí",      icon = "🎬", color = "#AB47BC", type = expense, isDefault = true, isArchived = false),
            CategoryEntity(userId = userId, name = "Hóa đơn",       icon = "💡", color = "#FFEE58", type = expense, isDefault = true, isArchived = false),
            CategoryEntity(userId = userId, name = "Sức khỏe",      icon = "🏥", color = "#EF5350", type = expense, isDefault = true, isArchived = false),
            CategoryEntity(userId = userId, name = "Giáo dục",      icon = "📚", color = "#5C6BC0", type = expense, isDefault = true, isArchived = false),
            CategoryEntity(userId = userId, name = "Du lịch",       icon = "✈️", color = "#29B6F6", type = expense, isDefault = true, isArchived = false),
            CategoryEntity(userId = userId, name = "Tạp hóa",       icon = "🛒", color = "#66BB6A", type = expense, isDefault = true, isArchived = false),
            CategoryEntity(userId = userId, name = "Khác",          icon = "📦", color = "#78909C", type = expense, isDefault = true, isArchived = false),
            CategoryEntity(userId = userId, name = "Lương",         icon = "💼", color = "#43A047", type = income,  isDefault = true, isArchived = false),
            CategoryEntity(userId = userId, name = "Thưởng",        icon = "🎁", color = "#EC407A", type = income,  isDefault = true, isArchived = false),
            CategoryEntity(userId = userId, name = "Đầu tư",        icon = "📈", color = "#26A69A", type = income,  isDefault = true, isArchived = false),
            CategoryEntity(userId = userId, name = "Thu nhập khác", icon = "💰", color = "#FF9800", type = income,  isDefault = true, isArchived = false),
        )
    }

    companion object {
        private const val TAG = "FirestoreSync"
    }
}
