package com.expensetracker.app.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.expensetracker.app.domain.model.RecurrenceFrequency
import com.expensetracker.app.domain.model.TransactionType
import java.time.LocalDate

@Entity(
    tableName = "recurring_transactions",
    foreignKeys = [
        ForeignKey(
            entity = WalletEntity::class,
            parentColumns = ["id"],
            childColumns = ["walletId"],
            onDelete = ForeignKey.CASCADE,
        ),
        ForeignKey(
            entity = CategoryEntity::class,
            parentColumns = ["id"],
            childColumns = ["categoryId"],
            onDelete = ForeignKey.RESTRICT,
        ),
    ],
    indices = [Index("userId"), Index("walletId"), Index("categoryId")],
)
data class RecurringTransactionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val userId: String = "",
    val walletId: Long,
    val categoryId: Long,
    val amount: Double,
    val type: TransactionType,
    val note: String,
    val frequency: RecurrenceFrequency,
    val interval: Int,
    val startDate: LocalDate,
    val endDate: LocalDate?,
    val nextOccurrence: LocalDate,
    val lastProcessed: LocalDate?,
    val isActive: Boolean,
)
