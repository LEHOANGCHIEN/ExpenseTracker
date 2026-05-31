package com.expensetracker.app.data.mapper

import com.expensetracker.app.data.local.entity.CategoryEntity
import com.expensetracker.app.domain.model.Category

fun CategoryEntity.toDomain() = Category(
    id = id,
    name = name,
    icon = icon,
    color = color,
    type = type,
    isDefault = isDefault,
    isArchived = isArchived,
)

fun Category.toEntity() = CategoryEntity(
    id = id,
    name = name,
    icon = icon,
    color = color,
    type = type,
    isDefault = isDefault,
    isArchived = isArchived,
)
