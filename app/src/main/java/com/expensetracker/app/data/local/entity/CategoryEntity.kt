package com.expensetracker.app.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.expensetracker.app.domain.model.TransactionType

@Entity(tableName = "categories")
data class CategoryEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val icon: String,
    val color: String,
    val type: TransactionType,
    val isDefault: Boolean,
    val isArchived: Boolean,
)
