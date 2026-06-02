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
            Triple("Ăn uống", "🍔", "#FFA726") to expense,
            Triple("Di chuyển", "🚗", "#42A5F5") to expense,
            Triple("Mua sắm", "🛍️", "#EC407A") to expense,
            Triple("Giải trí", "🎬", "#AB47BC") to expense,
            Triple("Hóa đơn", "💡", "#FFEE58") to expense,
            Triple("Sức khỏe", "🏥", "#EF5350") to expense,
            Triple("Giáo dục", "📚", "#5C6BC0") to expense,
            Triple("Du lịch", "✈️", "#29B6F6") to expense,
            Triple("Tạp hóa", "🛒", "#66BB6A") to expense,
            Triple("Khác", "📦", "#78909C") to expense,

            // Income categories
            Triple("Lương", "💼", "#43A047") to income,
            Triple("Thưởng", "🎁", "#EC407A") to income,
            Triple("Đầu tư", "📈", "#26A69A") to income,
            Triple("Thu nhập khác", "💰", "#FF9800") to income,
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
