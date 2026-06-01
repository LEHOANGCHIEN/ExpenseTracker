package com.expensetracker.app.feature.ocr_scan

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.expensetracker.app.data.mlkit.ReceiptOcrService
import com.expensetracker.app.data.mlkit.ReceiptParser
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ReceiptScannerViewModel @Inject constructor(
    private val ocrService: ReceiptOcrService,
    private val receiptParser: ReceiptParser,
) : ViewModel() {

    private val _state = MutableStateFlow<ReceiptScannerUiState>(ReceiptScannerUiState.Camera)
    val state: StateFlow<ReceiptScannerUiState> = _state

    fun processImage(uri: Uri, context: Context) {
        _state.value = ReceiptScannerUiState.Processing
        viewModelScope.launch {
            ocrService.recognize(uri, context).fold(
                onSuccess = { text ->
                    val parsed = receiptParser.parse(text)
                    _state.value = ReceiptScannerUiState.Result(parsed)
                },
                onFailure = { error ->
                    _state.value = ReceiptScannerUiState.Error(
                        error.message ?: "Failed to process image"
                    )
                },
            )
        }
    }

    fun reset() {
        _state.value = ReceiptScannerUiState.Camera
    }
}
