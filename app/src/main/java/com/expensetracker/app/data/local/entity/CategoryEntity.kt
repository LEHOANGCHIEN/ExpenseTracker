package com.expensetracker.app.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.expensetracker.app.domain.model.TransactionType

@Entity(
    tableName = "categories",
    indices = [Index("userId")],
)
data class CategoryEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val userId: String = "",
    val name: String,
    val icon: String,
    val color: String,
    val type: TransactionType,
    val isDefault: Boolean,
    val isArchived: Boolean,
)
