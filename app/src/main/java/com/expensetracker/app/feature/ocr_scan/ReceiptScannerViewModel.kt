package com.expensetracker.app.feature.ocr_scan

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.expensetracker.app.data.mlkit.ReceiptOcrService
import com.expensetracker.app.data.mlkit.ReceiptParser
import com.expensetracker.app.data.remote.gemini.GeminiApiService
import com.expensetracker.app.data.remote.gemini.GeminiPrompts
import com.expensetracker.app.data.remote.gemini.ReceiptParseResultDto
import com.expensetracker.app.domain.repository.AiRepository
import com.expensetracker.app.domain.repository.CategoryRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import java.time.LocalDate
import javax.inject.Inject

@HiltViewModel
class ReceiptScannerViewModel @Inject constructor(
    private val ocrService: ReceiptOcrService,
    private val receiptParser: ReceiptParser,
    private val geminiApiService: GeminiApiService,
    private val aiRepository: AiRepository,
    private val categoryRepository: CategoryRepository,
) : ViewModel() {

    private val lenientJson = Json { ignoreUnknownKeys = true; isLenient = true }

    private val _state = MutableStateFlow<ReceiptScannerUiState>(ReceiptScannerUiState.Camera)
    val state: StateFlow<ReceiptScannerUiState> = _state

    fun processImage(uri: Uri, context: Context) {
        _state.value = ReceiptScannerUiState.Processing
        viewModelScope.launch {
            ocrService.recognize(uri, context).fold(
                onSuccess = { rawText ->
                    var parsed = receiptParser.parse(rawText)

                    // Gemini fallback for low-confidence parses
                    if (parsed.confidence < 0.5f && geminiApiService.isApiKeyAvailable()) {
                        val prompt = GeminiPrompts.buildReceiptParsePrompt(rawText)
                        geminiApiService.generateJson(systemInstruction = prompt)
                            .getOrNull()
                            ?.let { json ->
                                runCatching {
                                    val dto = lenientJson.decodeFromString<ReceiptParseResultDto>(json)
                                    parsed = parsed.copy(
                                        amount = dto.amount ?: parsed.amount,
                                        date = dto.date?.let {
                                            runCatching { LocalDate.parse(it) }.getOrNull()
                                        } ?: parsed.date,
                                        merchant = dto.merchant ?: parsed.merchant,
                                        confidence = 0.7f,
                                    )
                                }
                            }
                    }

                    // AI category suggestion based on merchant name
                    val suggestedCategoryId = if (parsed.merchant.isNotBlank()) {
                        runCatching {
                            val categories = categoryRepository.observeAll().first()
                            aiRepository.categorizeTransaction(
                                note = parsed.merchant,
                                amount = parsed.amount ?: 0.0,
                                categories = categories,
                            ).getOrNull()
                        }.getOrNull()
                    } else null

                    _state.value = ReceiptScannerUiState.Result(
                        ReceiptResultData(
                            parsed = parsed,
                            capturedImageUri = uri,
                            suggestedCategoryId = suggestedCategoryId,
                        ),
                    )
                },
                onFailure = { error ->
                    _state.value = ReceiptScannerUiState.Error(
                        error.message ?: "Failed to read receipt. Please try again.",
                    )
                },
            )
        }
    }

    fun onAmountEdited(value: String) {
        updateResult { it.copy(editedAmount = value) }
    }

    fun onNoteEdited(value: String) {
        updateResult { it.copy(editedNote = value) }
    }

    fun onDateEdited(value: String) {
        updateResult { it.copy(editedDateStr = value) }
    }

    fun reset() {
        _state.value = ReceiptScannerUiState.Camera
    }

    private inline fun updateResult(transform: (ReceiptResultData) -> ReceiptResultData) {
        val current = _state.value as? ReceiptScannerUiState.Result ?: return
        _state.value = current.copy(data = transform(current.data))
    }
}
