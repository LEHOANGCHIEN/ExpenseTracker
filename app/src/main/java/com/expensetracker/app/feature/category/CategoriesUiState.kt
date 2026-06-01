package com.expensetracker.app.feature.category

import com.expensetracker.app.domain.model.Category
import com.expensetracker.app.domain.model.TransactionType

data class CategoryWithSpending(
    val category: Category,
    val monthlySpending: Double = 0.0,
)

data class CategoriesUiState(
    val expenseCategories: List<CategoryWithSpending> = emptyList(),
    val incomeCategories: List<CategoryWithSpending> = emptyList(),
    val selectedTab: TransactionType = TransactionType.EXPENSE,
    val longPressedCategory: Category? = null,
    val deleteBlockedCategory: Category? = null,
    val isLoading: Boolean = true,
    val currency: String = "VND",
) {
    val currentTabCategories: List<CategoryWithSpending>
        get() = if (selectedTab == TransactionType.EXPENSE) expenseCategories else incomeCategories
}

sealed interface CategoriesEvent {
    data class TabSelected(val type: TransactionType) : CategoriesEvent
    data class LongPressCategory(val category: Category) : CategoriesEvent
    data object DismissActionSheet : CategoriesEvent
    data class ArchiveCategory(val id: Long) : CategoriesEvent
    data class DeleteCategory(val id: Long) : CategoriesEvent
    data object DismissDeleteBlocked : CategoriesEvent
}
