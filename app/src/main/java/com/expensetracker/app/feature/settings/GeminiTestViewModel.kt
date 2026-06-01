package com.expensetracker.app.feature.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.expensetracker.app.data.remote.gemini.GeminiApiService
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class GeminiTestUiState(
    val prompt: String = "",
    val response: String = "",
    val isLoading: Boolean = false,
    val error: String? = null,
    val apiKeyAvailable: Boolean = false,
)

@HiltViewModel
class GeminiTestViewModel @Inject constructor(
    private val geminiApiService: GeminiApiService,
) : ViewModel() {

    private val _uiState = MutableStateFlow(
        GeminiTestUiState(apiKeyAvailable = geminiApiService.isApiKeyAvailable()),
    )
    val uiState: StateFlow<GeminiTestUiState> = _uiState.asStateFlow()

    fun onPromptChanged(prompt: String) {
        _uiState.update { it.copy(prompt = prompt, error = null) }
    }

    fun sendPrompt() {
        val prompt = _uiState.value.prompt.trim()
        if (prompt.isBlank()) return

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, response = "", error = null) }
            geminiApiService.sendPrompt(prompt)
                .onSuccess { response ->
                    _uiState.update { it.copy(isLoading = false, response = response) }
                }
                .onFailure { e ->
                    _uiState.update {
                        it.copy(isLoading = false, error = e.message ?: "Unknown error")
                    }
                }
        }
    }

    fun clearResponse() {
        _uiState.update { it.copy(response = "", error = null, prompt = "") }
    }
}
