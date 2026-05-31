package com.expensetracker.app.data.local.database

import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import java.time.LocalDateTime

object DatabaseSeeder {

    fun callback(): RoomDatabase.Callback = object : RoomDatabase.Callback() {
        override fun onCreate(db: SupportSQLiteDatabase) {
            super.onCreate(db)
            val now = LocalDateTime.now().toString()
            seedWallet(db, now)
            seedCategories(db)
        }
    }

    private fun seedWallet(db: SupportSQLiteDatabase, now: String) {
        db.execSQL(
            """INSERT INTO wallets (name, icon, color, initialBalance, currency, createdAt)
               VALUES ('Cash', '💵', '#26A69A', 0.0, 'VND', '$now')"""
        )
    }

    private fun seedCategories(db: SupportSQLiteDatabase) {
        val expense = "EXPENSE"
        val income = "INCOME"

        val categories = listOf(
            // Expense categories
            Triple("Food", "🍔", "#FFA726") to expense,
            Triple("Transport", "🚗", "#42A5F5") to expense,
            Triple("Shopping", "🛍️", "#EC407A") to expense,
            Triple("Entertainment", "🎬", "#AB47BC") to expense,
            Triple("Bills", "💡", "#FFEE58") to expense,
            Triple("Health", "🏥", "#EF5350") to expense,
            Triple("Education", "📚", "#5C6BC0") to expense,
            Triple("Travel", "✈️", "#29B6F6") to expense,
            Triple("Groceries", "🛒", "#66BB6A") to expense,
            Triple("Other", "📦", "#78909C") to expense,
            // Income categories
            Triple("Salary", "💼", "#43A047") to income,
            Triple("Bonus", "🎁", "#EC407A") to income,
            Triple("Investment", "📈", "#26A69A") to income,
            Triple("Other Income", "💰", "#FF9800") to income,
        )

        categories.forEach { (info, type) ->
            val (name, icon, color) = info
            db.execSQL(
                """INSERT INTO categories (name, icon, color, type, isDefault, isArchived)
                   VALUES (?, ?, ?, ?, 1, 0)""",
                arrayOf(name, icon, color, type),
            )
        }
    }
}
