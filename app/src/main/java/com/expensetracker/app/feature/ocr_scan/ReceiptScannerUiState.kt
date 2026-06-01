package com.expensetracker.app.feature.ocr_scan

import com.expensetracker.app.data.mlkit.ParsedReceipt

sealed interface ReceiptScannerUiState {
    data object Camera : ReceiptScannerUiState
    data object Processing : ReceiptScannerUiState
    data class Result(val parsed: ParsedReceipt) : ReceiptScannerUiState
    data class Error(val message: String) : ReceiptScannerUiState
}
