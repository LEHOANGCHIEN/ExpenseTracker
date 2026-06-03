package com.expensetracker.app.data.bootstrap

import com.expensetracker.app.data.local.dao.CategoryDao
import com.expensetracker.app.data.local.dao.WalletDao
import com.expensetracker.app.data.local.entity.CategoryEntity
import com.expensetracker.app.data.local.entity.WalletEntity
import com.expensetracker.app.domain.model.TransactionType
import java.time.LocalDateTime
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserBootstrapService @Inject constructor(
    private val categoryDao: CategoryDao,
    private val walletDao: WalletDao,
) {
    suspend fun bootstrapIfNeeded(userId: String) {
        if (categoryDao.countByUserId(userId) > 0) return
        seedDefaultWallet(userId)
        seedDefaultCategories(userId)
    }

    private suspend fun seedDefaultWallet(userId: String) {
        walletDao.insert(
            WalletEntity(
                userId = userId,
                name = "Cash",
                icon = "💵",
                color = "#26A69A",
                initialBalance = 0.0,
                currency = "VND",
                createdAt = LocalDateTime.now(),
            ),
        )
    }

    private suspend fun seedDefaultCategories(userId: String) {
        categoryDao.insertAll(defaultCategories(userId))
    }

    private fun defaultCategories(userId: String): List<CategoryEntity> {
        val expense = TransactionType.EXPENSE
        val income = TransactionType.INCOME
        return listOf(
            CategoryEntity(userId = userId, name = "Ăn uống",      icon = "🍔", color = "#FFA726", type = expense, isDefault = true, isArchived = false),
            CategoryEntity(userId = userId, name = "Di chuyển",    icon = "🚗", color = "#42A5F5", type = expense, isDefault = true, isArchived = false),
            CategoryEntity(userId = userId, name = "Mua sắm",      icon = "🛍️", color = "#EC407A", type = expense, isDefault = true, isArchived = false),
            CategoryEntity(userId = userId, name = "Giải trí",     icon = "🎬", color = "#AB47BC", type = expense, isDefault = true, isArchived = false),
            CategoryEntity(userId = userId, name = "Hóa đơn",      icon = "💡", color = "#FFEE58", type = expense, isDefault = true, isArchived = false),
            CategoryEntity(userId = userId, name = "Sức khỏe",     icon = "🏥", color = "#EF5350", type = expense, isDefault = true, isArchived = false),
            CategoryEntity(userId = userId, name = "Giáo dục",     icon = "📚", color = "#5C6BC0", type = expense, isDefault = true, isArchived = false),
            CategoryEntity(userId = userId, name = "Du lịch",      icon = "✈️", color = "#29B6F6", type = expense, isDefault = true, isArchived = false),
            CategoryEntity(userId = userId, name = "Tạp hóa",      icon = "🛒", color = "#66BB6A", type = expense, isDefault = true, isArchived = false),
            CategoryEntity(userId = userId, name = "Khác",         icon = "📦", color = "#78909C", type = expense, isDefault = true, isArchived = false),
            CategoryEntity(userId = userId, name = "Lương",        icon = "💼", color = "#43A047", type = income,  isDefault = true, isArchived = false),
            CategoryEntity(userId = userId, name = "Thưởng",       icon = "🎁", color = "#EC407A", type = income,  isDefault = true, isArchived = false),
            CategoryEntity(userId = userId, name = "Đầu tư",       icon = "📈", color = "#26A69A", type = income,  isDefault = true, isArchived = false),
            CategoryEntity(userId = userId, name = "Thu nhập khác",icon = "💰", color = "#FF9800", type = income,  isDefault = true, isArchived = false),
        )
    }
}
