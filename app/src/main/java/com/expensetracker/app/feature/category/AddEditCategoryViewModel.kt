package com.expensetracker.app.feature.category

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.expensetracker.app.domain.model.Category
import com.expensetracker.app.domain.model.TransactionType
import com.expensetracker.app.domain.repository.CategoryRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AddEditCategoryViewModel @Inject constructor(
    private val categoryRepository: CategoryRepository,
    savedStateHandle: SavedStateHandle,
) : ViewModel() {

    private val categoryId: Long? = savedStateHandle.get<Long?>("id")

    private val _state = MutableStateFlow(AddEditCategoryUiState())
    val state: StateFlow<AddEditCategoryUiState> = _state

    init {
        if (categoryId != null) {
            viewModelScope.launch {
                val cat = categoryRepository.getById(categoryId)
                if (cat != null) {
                    _state.update {
                        it.copy(
                            categoryId = cat.id,
                            name = cat.name,
                            icon = cat.icon,
                            color = cat.color,
                            type = cat.type,
                            isDefault = cat.isDefault,
                            isEditing = true,
                        )
                    }
                }
            }
        }
    }

    fun onEvent(event: AddEditCategoryEvent) {
        when (event) {
            is AddEditCategoryEvent.NameChanged -> _state.update { it.copy(name = event.name.take(40)) }
            is AddEditCategoryEvent.TypeChanged -> _state.update { it.copy(type = event.type) }
            is AddEditCategoryEvent.IconSelected -> _state.update {
                it.copy(icon = event.icon, showIconPicker = false)
            }
            is AddEditCategoryEvent.ColorSelected -> _state.update {
                it.copy(color = event.color, showColorPicker = false)
            }
            AddEditCategoryEvent.ShowIconPicker -> _state.update { it.copy(showIconPicker = true) }
            AddEditCategoryEvent.HideIconPicker -> _state.update { it.copy(showIconPicker = false) }
            AddEditCategoryEvent.ShowColorPicker -> _state.update { it.copy(showColorPicker = true) }
            AddEditCategoryEvent.HideColorPicker -> _state.update { it.copy(showColorPicker = false) }
            AddEditCategoryEvent.Save -> save()
        }
    }

    private fun save() {
        val s = _state.value
        if (s.name.isBlank()) return
        _state.update { it.copy(isSaving = true, error = null) }
        viewModelScope.launch {
            try {
                val category = Category(
                    id = s.categoryId ?: 0L,
                    name = s.name.trim(),
                    icon = s.icon,
                    color = s.color,
                    type = s.type,
                    isDefault = s.isDefault,
                    isArchived = false,
                )
                if (s.isEditing) {
                    categoryRepository.update(category)
                } else {
                    categoryRepository.add(category)
                }
                _state.update { it.copy(isSaving = false, isDone = true) }
            } catch (e: Exception) {
                _state.update { it.copy(isSaving = false, error = e.message) }
            }
        }
    }
}
