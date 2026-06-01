package com.expensetracker.app.data.mlkit

import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException
import javax.inject.Inject
import javax.inject.Singleton

data class ParsedReceipt(
    val amount: Double?,
    val date: LocalDate,
    val merchant: String,
    val rawText: String,
    val confidence: Float,
)

@Singleton
class ReceiptParser @Inject constructor() {

    private val totalKeywords = listOf(
        "TOTAL", "AMOUNT", "TỔNG", "TỔNG TIỀN", "THÀNH TIỀN",
        "GRAND TOTAL", "SUBTOTAL", "SUB-TOTAL", "AMOUNT DUE", "BALANCE DUE",
    )

    private val dateFormats = listOf(
        DateTimeFormatter.ofPattern("dd/MM/yyyy"),
        DateTimeFormatter.ofPattern("MM/dd/yyyy"),
        DateTimeFormatter.ofPattern("yyyy-MM-dd"),
        DateTimeFormatter.ofPattern("dd-MM-yyyy"),
        DateTimeFormatter.ofPattern("dd.MM.yyyy"),
    )

    fun parse(ocrText: String): ParsedReceipt {
        val lines = ocrText.lines().map { it.trim() }.filter { it.isNotBlank() }

        val merchant = extractMerchant(lines)
        val amount = extractAmount(lines)
        val date = extractDate(lines)

        val confidence = when {
            amount != null && date != LocalDate.now() -> 0.85f
            amount != null -> 0.65f
            else -> 0.3f
        }

        return ParsedReceipt(
            amount = amount,
            date = date,
            merchant = merchant,
            rawText = ocrText,
            confidence = confidence,
        )
    }

    private fun extractMerchant(lines: List<String>): String {
        // Take first non-empty line that looks like a name (not just numbers)
        return lines.take(3)
            .firstOrNull { line ->
                line.length > 2 && !line.all { it.isDigit() || it == '.' || it == ',' || it == ':' }
            }
            ?.take(50) ?: "Unknown Merchant"
    }

    private fun extractAmount(lines: List<String>): Double? {
        // Try to find amount near total keywords
        for (i in lines.indices) {
            val line = lines[i].uppercase()
            val isNearKeyword = totalKeywords.any { keyword -> line.contains(keyword) }

            if (isNearKeyword) {
                // Try current line first, then next line
                val candidateLines = buildList {
                    add(lines[i])
                    if (i + 1 < lines.size) add(lines[i + 1])
                }
                for (candidate in candidateLines) {
                    val amount = parseAmount(candidate)
                    if (amount != null && amount > 0) return amount
                }
            }
        }

        // Fallback: find the largest number that looks like a price
        return lines
            .mapNotNull { parseAmount(it) }
            .filter { it > 0 }
            .maxOrNull()
    }

    private fun parseAmount(text: String): Double? {
        // Remove currency symbols and whitespace, keep digits and separators
        val cleaned = text.replace(Regex("[₫$€£¥\\s]"), "")
        // Try to extract number patterns like 1,234,567 or 1.234.567 or 1234567
        val numberPattern = Regex("[\\d]{1,3}(?:[.,][\\d]{3})*(?:[.,][\\d]{1,2})?|[\\d]+")
        val matches = numberPattern.findAll(cleaned)
        return matches
            .mapNotNull { match ->
                val numStr = match.value
                    .replace(",", "")
                    .replace(".", "")
                numStr.toDoubleOrNull()
            }
            .filter { it > 100 } // Filter out small numbers (not likely prices)
            .maxOrNull()
    }

    private fun extractDate(lines: List<String>): LocalDate {
        val datePattern = Regex("\\d{1,2}[/\\-.]\\d{1,2}[/\\-.]\\d{2,4}|\\d{4}[/\\-.]\\d{1,2}[/\\-.]\\d{1,2}")
        for (line in lines) {
            val match = datePattern.find(line) ?: continue
            for (fmt in dateFormats) {
                try {
                    return LocalDate.parse(match.value, fmt)
                } catch (e: DateTimeParseException) {
                    continue
                }
            }
        }
        return LocalDate.now()
    }
}
