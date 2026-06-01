package com.expensetracker.app.feature.category

import com.expensetracker.app.domain.model.TransactionType

data class AddEditCategoryUiState(
    val categoryId: Long? = null,
    val name: String = "",
    val icon: String = "💡",
    val color: String = "#26A69A",
    val type: TransactionType = TransactionType.EXPENSE,
    val isDefault: Boolean = false,
    val isEditing: Boolean = false,
    val isSaving: Boolean = false,
    val isDone: Boolean = false,
    val showIconPicker: Boolean = false,
    val showColorPicker: Boolean = false,
    val error: String? = null,
) {
    val canSave: Boolean get() = name.isNotBlank() && !isSaving
}

sealed interface AddEditCategoryEvent {
    data class NameChanged(val name: String) : AddEditCategoryEvent
    data class TypeChanged(val type: TransactionType) : AddEditCategoryEvent
    data class IconSelected(val icon: String) : AddEditCategoryEvent
    data class ColorSelected(val color: String) : AddEditCategoryEvent
    data object ShowIconPicker : AddEditCategoryEvent
    data object HideIconPicker : AddEditCategoryEvent
    data object ShowColorPicker : AddEditCategoryEvent
    data object HideColorPicker : AddEditCategoryEvent
    data object Save : AddEditCategoryEvent
}
