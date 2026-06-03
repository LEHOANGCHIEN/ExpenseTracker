package com.expensetracker.app.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import java.time.LocalDateTime

@Entity(
    tableName = "wallets",
    indices = [Index("userId")],
)
data class WalletEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val userId: String = "",
    val name: String,
    val icon: String,
    val color: String,
    val initialBalance: Double,
    val currency: String,
    val createdAt: LocalDateTime,
)
