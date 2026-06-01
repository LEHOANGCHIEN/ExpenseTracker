package com.expensetracker.app.feature.ocr_scan

import android.net.Uri
import com.expensetracker.app.data.mlkit.ParsedReceipt
import java.time.format.DateTimeFormatter

data class ReceiptResultData(
    val parsed: ParsedReceipt,
    val capturedImageUri: Uri? = null,
    val suggestedCategoryId: Long? = null,
    val editedAmount: String = parsed.amount?.toLong()?.toString() ?: "",
    val editedNote: String = parsed.merchant,
    val editedDateStr: String = parsed.date.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")),
)

sealed interface ReceiptScannerUiState {
    data object Camera : ReceiptScannerUiState
    data object Processing : ReceiptScannerUiState
    data class Result(val data: ReceiptResultData) : ReceiptScannerUiState
    data class Error(val message: String) : ReceiptScannerUiState
}
