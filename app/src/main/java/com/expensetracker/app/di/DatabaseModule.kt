package com.expensetracker.app.di

import android.content.Context
import androidx.room.Room
import com.expensetracker.app.data.local.dao.AiChatMessageDao
import com.expensetracker.app.data.local.dao.AiInsightDao
import com.expensetracker.app.data.local.dao.BudgetDao
import com.expensetracker.app.data.local.dao.CategoryDao
import com.expensetracker.app.data.local.dao.RecurringTransactionDao
import com.expensetracker.app.data.local.dao.TransactionDao
import com.expensetracker.app.data.local.dao.WalletDao
import com.expensetracker.app.data.local.database.AppDatabase
import com.expensetracker.app.data.local.database.DatabaseSeeder
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideAppDatabase(@ApplicationContext context: Context): AppDatabase =
        Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "expense_tracker.db",
        )
            .addCallback(DatabaseSeeder.callback())
            .fallbackToDestructiveMigration(dropAllTables = true)
            .build()

    @Provides
    @Singleton
    fun provideWalletDao(db: AppDatabase): WalletDao = db.walletDao()

    @Provides
    @Singleton
    fun provideCategoryDao(db: AppDatabase): CategoryDao = db.categoryDao()

    @Provides
    @Singleton
    fun provideTransactionDao(db: AppDatabase): TransactionDao = db.transactionDao()

    @Provides
    @Singleton
    fun provideBudgetDao(db: AppDatabase): BudgetDao = db.budgetDao()

    @Provides
    @Singleton
    fun provideRecurringTransactionDao(db: AppDatabase): RecurringTransactionDao =
        db.recurringTransactionDao()

    @Provides
    @Singleton
    fun provideAiChatMessageDao(db: AppDatabase): AiChatMessageDao = db.aiChatMessageDao()

    @Provides
    @Singleton
    fun provideAiInsightDao(db: AppDatabase): AiInsightDao = db.aiInsightDao()
}
