package com.expensetracker.app.feature.ocr_scan.screen.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SheetState
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.SuggestionChipDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.expensetracker.app.core.designsystem.theme.IncomeGreen
import com.expensetracker.app.core.designsystem.theme.WarningOrange
import com.expensetracker.app.core.designsystem.theme.ExpenseRed
import com.expensetracker.app.domain.model.Category
import com.expensetracker.app.feature.ocr_scan.ReceiptResultData

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OcrResultSheet(
    data: ReceiptResultData,
    suggestedCategory: Category?,
    sheetState: SheetState,
    onDismiss: () -> Unit,
    onAmountChanged: (String) -> Unit,
    onNoteChanged: (String) -> Unit,
    onDateChanged: (String) -> Unit,
    onUseThis: () -> Unit,
    onScanAgain: () -> Unit,
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp)
                .padding(bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            // Header
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(
                    text = "Receipt Scanned",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f),
                )
                ConfidenceBadge(confidence = data.parsed.confidence)
            }

            // Captured image preview
            if (data.capturedImageUri != null) {
                AsyncImage(
                    model = data.capturedImageUri,
                    contentDescription = "Captured receipt",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp),
                )
            }

            // Editable fields
            OutlinedTextField(
                value = data.editedNote,
                onValueChange = onNoteChanged,
                label = { Text("Merchant / Note") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
            )

            OutlinedTextField(
                value = data.editedAmount,
                onValueChange = onAmountChanged,
                label = { Text("Amount") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
            )

            OutlinedTextField(
                value = data.editedDateStr,
                onValueChange = onDateChanged,
                label = { Text("Date (dd/MM/yyyy)") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
            )

            // Category suggestion
            if (suggestedCategory != null) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "Suggested category:",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Spacer(Modifier.width(8.dp))
                    SuggestionChip(
                        onClick = {},
                        label = {
                            Text("${suggestedCategory.icon} ${suggestedCategory.name}")
                        },
                        colors = SuggestionChipDefaults.suggestionChipColors(
                            containerColor = MaterialTheme.colorScheme.secondaryContainer,
                        ),
                    )
                }
            }

            Spacer(Modifier.height(4.dp))

            // Action buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                OutlinedButton(
                    onClick = onScanAgain,
                    modifier = Modifier.weight(1f),
                ) {
                    Text("Scan Again")
                }
                Button(
                    onClick = onUseThis,
                    modifier = Modifier.weight(1f),
                ) {
                    Text("Use This")
                }
            }
        }
    }
}

@Composable
private fun ConfidenceBadge(confidence: Float) {
    val (label, color) = when {
        confidence >= 0.8f -> "High confidence" to IncomeGreen
        confidence >= 0.5f -> "Medium confidence" to WarningOrange
        else -> "Low confidence" to ExpenseRed
    }
    SuggestionChip(
        onClick = {},
        label = { Text(label, style = MaterialTheme.typography.labelSmall) },
        colors = SuggestionChipDefaults.suggestionChipColors(
            containerColor = color.copy(alpha = 0.15f),
            labelColor = color,
        ),
    )
}
