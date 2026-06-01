package com.expensetracker.app.data.local.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.expensetracker.app.data.local.dao.AiChatMessageDao
import com.expensetracker.app.data.local.dao.AiInsightDao
import com.expensetracker.app.data.local.dao.BudgetDao
import com.expensetracker.app.data.local.dao.CategoryDao
import com.expensetracker.app.data.local.dao.RecurringTransactionDao
import com.expensetracker.app.data.local.dao.TransactionDao
import com.expensetracker.app.data.local.dao.WalletDao
import com.expensetracker.app.data.local.entity.AiChatMessageEntity
import com.expensetracker.app.data.local.entity.AiInsightEntity
import com.expensetracker.app.data.local.entity.BudgetEntity
import com.expensetracker.app.data.local.entity.CategoryEntity
import com.expensetracker.app.data.local.entity.RecurringTransactionEntity
import com.expensetracker.app.data.local.entity.TransactionEntity
import com.expensetracker.app.data.local.entity.WalletEntity

@Database(
    entities = [
        WalletEntity::class,
        CategoryEntity::class,
        RecurringTransactionEntity::class,
        TransactionEntity::class,
        BudgetEntity::class,
        AiChatMessageEntity::class,
        AiInsightEntity::class,
    ],
    version = 2,
    exportSchema = true,
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun walletDao(): WalletDao
    abstract fun categoryDao(): CategoryDao
    abstract fun transactionDao(): TransactionDao
    abstract fun budgetDao(): BudgetDao
    abstract fun recurringTransactionDao(): RecurringTransactionDao
    abstract fun aiChatMessageDao(): AiChatMessageDao
    abstract fun aiInsightDao(): AiInsightDao
}
