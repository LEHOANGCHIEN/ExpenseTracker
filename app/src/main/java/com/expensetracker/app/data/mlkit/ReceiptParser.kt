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

    private enum class AmountQuality { TOTAL, SUBTOTAL, FALLBACK, NONE }

    // Unambiguous "amount the customer pays" — highest priority
    private val totalKeywords = listOf(
        "TIỀN THANH TOÁN", "TỔNG TIỀN THANH TOÁN", "TỔNG SỐ TIỀN THANH TOÁN",
        "TỔNG CỘNG", "TỔNG SỐ TIỀN", "KHÁCH TRẢ", "SỐ TIỀN THANH TOÁN",
        "GRAND TOTAL", "TOTAL AMOUNT", "AMOUNT DUE", "BALANCE DUE",
        "AMOUNT PAYABLE", "NET TOTAL", "TOTAL DUE",
    )

    // Potentially ambiguous — may be pre-tax subtotals on some receipts
    private val subtotalKeywords = listOf(
        "THÀNH TIỀN", "TỔNG TIỀN", "TỔNG TIỀN HÀNG",
        "TOTAL", "SUBTOTAL", "SUB-TOTAL", "SUB TOTAL",
        "TỔNG", "AMOUNT",
    )

    // Lines matching any of these must never be used as the payable total
    private val excludeAmountKeywords = listOf(
        "VAT", "THUẾ", "GTGT", "TAX",
        "DISCOUNT", "GIẢM GIÁ", "GIẢM", "COUPON", "KHUYẾN MÃI",
        "PHÍ DỊCH VỤ", "SERVICE CHARGE", "TIP",
        "ĐIỆN THOẠI", "ĐT:", "ĐT :", "TEL:", "PHONE:", "FAX:",
        "MÃ SỐ THUẾ", "MST:", "MST :", "TAX CODE",
        "SỐ HÓA ĐƠN", "INVOICE NO", "ORDER NO", "ĐƠN HÀNG",
    )

    // Generic receipt-header text that is not a merchant name
    private val receiptHeaderNoise = listOf(
        "HÓA ĐƠN", "HOÁ ĐƠN", "PHIẾU THU", "PHIẾU TÍNH TIỀN", "PHIẾU CHI",
        "RECEIPT", "INVOICE", "ORDER", "BILL",
        "BÁN HÀNG", "THANK YOU", "XIN CẢM ƠN", "CẢM ƠN",
        "WELCOME", "XIN CHÀO", "CHÀO MỪNG",
    )

    // Prefixes or brand names that strongly indicate the merchant line
    private val businessPrefixes = listOf(
        "SIÊU THỊ", "CỬA HÀNG", "NHÀ HÀNG", "QUÁN", "CAFÉ", "CAFE",
        "CÔNG TY", "CTY TNHH", "CTY CP", "CTY", "CHI NHÁNH", "TRUNG TÂM",
        "CO.OPMART", "COOPMART", "VINMART", "BÁCH HÓA XANH", "LOTTE MART",
        "CIRCLE K", "7-ELEVEN", "MINISTOP", "GUARDIAN", "PHARMACITY",
        "THE COFFEE HOUSE", "HIGHLANDS", "STARBUCKS", "PHÚC LONG",
        "GRAB", "SHOPEEFOOD", "GOFOOD",
    )

    private val dateFormats = listOf(
        DateTimeFormatter.ofPattern("dd/MM/yyyy"),
        DateTimeFormatter.ofPattern("dd/MM/yy"),
        DateTimeFormatter.ofPattern("MM/dd/yyyy"),
        DateTimeFormatter.ofPattern("yyyy-MM-dd"),
        DateTimeFormatter.ofPattern("dd-MM-yyyy"),
        DateTimeFormatter.ofPattern("dd-MM-yy"),
        DateTimeFormatter.ofPattern("dd.MM.yyyy"),
        DateTimeFormatter.ofPattern("dd.MM.yy"),
    )

    // Matches "ngày 15 tháng 06 năm 2024" and unaccented variants from poor OCR
    private val viDatePattern = Regex(
        """(?:ngày|ngay)\s*(\d{1,2})\s*(?:tháng|thang|th\.?)\s*(\d{1,2})\s*(?:năm|nam)\s*(\d{2,4})""",
        RegexOption.IGNORE_CASE,
    )
    private val numericDatePattern = Regex(
        """\d{1,2}[/\-.]\d{1,2}[/\-.]\d{2,4}|\d{4}[/\-.]\d{1,2}[/\-.]\d{1,2}""",
    )

    // ---- Public API ----

    fun parse(ocrText: String): ParsedReceipt {
        val lines = ocrText.lines().map { it.trim() }.filter { it.isNotBlank() }

        val merchant = extractMerchant(lines)
        val (amount, amountQuality) = extractAmount(lines)
        val (date, dateFound) = extractDate(lines)

        return ParsedReceipt(
            amount = amount,
            date = date,
            merchant = merchant,
            rawText = ocrText,
            confidence = computeConfidence(merchant, amount, amountQuality, dateFound),
        )
    }

    // ---- Merchant ----

    private fun extractMerchant(lines: List<String>): String {
        val candidates = lines.take(7)

        // First pass: a line that contains a known business type/brand — most reliable
        val byPrefix = candidates.firstOrNull { line ->
            val upper = line.uppercase()
            businessPrefixes.any { upper.contains(it) }
        }
        if (byPrefix != null) return byPrefix.take(60)

        // Second pass: first line that is none of: header noise, address, phone/code, pure digits
        return candidates.firstOrNull { line ->
            val upper = line.uppercase()
            line.length > 3 &&
                receiptHeaderNoise.none { upper.contains(it) } &&
                !looksLikeAddress(upper) &&
                !looksLikePhoneOrCode(line) &&
                !line.all { c -> c.isDigit() || c == '.' || c == ',' || c == ':' || c == '-' || c == ' ' }
        }?.take(60) ?: "Unknown Merchant"
    }

    private fun looksLikeAddress(upper: String): Boolean =
        upper.startsWith("ĐỊA CHỈ") || upper.startsWith("ADDRESS") ||
            upper.startsWith("ĐC:") || upper.startsWith("ĐC :") ||
            listOf("ĐƯỜNG", " PHƯỜNG", " QUẬN", " HUYỆN", "TỈNH", "THÀNH PHỐ", "TP.", " Q.", " P.")
                .any { upper.contains(it) }

    private fun looksLikePhoneOrCode(line: String): Boolean {
        val stripped = line.replace(Regex("""[\s\-()+.]"""), "")
        val digits = stripped.filter { it.isDigit() }
        // Phone / fax: 10+ digit strings that are almost entirely digits
        if (digits.length >= 10 && digits.length.toFloat() / stripped.length.coerceAtLeast(1) > 0.85f) return true
        // Invoice / order / tax-code lines: mostly digits
        if (line.isNotEmpty() && digits.length.toFloat() / line.length > 0.75f) return true
        return false
    }

    // ---- Amount ----

    private fun extractAmount(lines: List<String>): Pair<Double?, AmountQuality> {
        // Pass 1 — unambiguous "total to pay" keywords
        scanForAmount(lines, totalKeywords)?.let { return it to AmountQuality.TOTAL }

        // Pass 2 — ambiguous keywords (TOTAL, SUBTOTAL, TỔNG, …)
        scanForAmount(lines, subtotalKeywords)?.let { return it to AmountQuality.SUBTOTAL }

        // Pass 3 — fallback: largest plausible price on any non-noise line
        val fallback = lines
            .filter { line ->
                val upper = line.uppercase()
                excludeAmountKeywords.none { upper.contains(it) } &&
                    !looksLikePhoneOrCode(line)
            }
            .mapNotNull { parseAmountFromLine(it, minValue = 999.0) }
            .maxOrNull()

        return if (fallback != null) fallback to AmountQuality.FALLBACK
        else null to AmountQuality.NONE
    }

    private fun scanForAmount(lines: List<String>, keywords: List<String>): Double? {
        for (i in lines.indices) {
            val upper = lines[i].uppercase()
            if (excludeAmountKeywords.any { upper.contains(it) }) continue
            if (!keywords.any { upper.contains(it) }) continue

            // Check the keyword line itself, then the next line (amount often on the line below)
            val candidates = buildList {
                add(lines[i])
                if (i + 1 < lines.size) add(lines[i + 1])
            }
            for (candidate in candidates) {
                val amount = parseAmountFromLine(candidate, minValue = 0.0)
                if (amount != null) return amount
            }
        }
        return null
    }

    private fun parseAmountFromLine(text: String, minValue: Double): Double? {
        // Strip currency markers that immediately follow digits (e.g. "50.000đ") and standalone symbols
        val cleaned = text
            .replace(Regex("""(\d)đ""", RegexOption.IGNORE_CASE), "$1")
            .replace(Regex("""[₫$€£¥]"""), "")
            .replace("VNĐ", "", ignoreCase = true)
            .replace("VND", "", ignoreCase = true)
            .replace("đồng", "", ignoreCase = true)

        return Regex("""[\d][\d.,]*[\d]|[\d]+""")
            .findAll(cleaned)
            .mapNotNull { parseNumber(it.value) }
            .filter { it > minValue }
            .maxOrNull()
    }

    /**
     * Disambiguates thousands separators from decimal points.
     * Vietnamese VND: never has decimal places; dots/commas are thousands separators.
     * Western: single trailing separator with 1-2 digits is a decimal point.
     */
    private fun parseNumber(raw: String): Double? {
        val dotCount = raw.count { it == '.' }
        val commaCount = raw.count { it == ',' }
        return when {
            // Multiple separators of the same kind → all are thousands seps (1.234.567)
            dotCount > 1 || commaCount > 1 ->
                raw.replace(".", "").replace(",", "").toDoubleOrNull()

            // Both present → western decimal notation
            dotCount == 1 && commaCount == 1 -> {
                if (raw.lastIndexOf('.') > raw.lastIndexOf(','))
                    raw.replace(",", "").toDoubleOrNull()          // 1,234.56
                else
                    raw.replace(".", "").replace(",", ".").toDoubleOrNull() // 1.234,56
            }

            // Single dot: thousands sep when exactly 3 digits follow (1.234), else decimal
            dotCount == 1 ->
                if (raw.substringAfterLast('.').length == 3) raw.replace(".", "").toDoubleOrNull()
                else raw.toDoubleOrNull()

            // Single comma: thousands sep when exactly 3 digits follow (1,234), else decimal
            commaCount == 1 ->
                if (raw.substringAfterLast(',').length == 3) raw.replace(",", "").toDoubleOrNull()
                else raw.replace(",", ".").toDoubleOrNull()

            else -> raw.toDoubleOrNull()
        }
    }

    // ---- Date ----

    private fun extractDate(lines: List<String>): Pair<LocalDate, Boolean> {
        val now = LocalDate.now()

        // Vietnamese textual format: "ngày 15 tháng 06 năm 2024"
        for (line in lines) {
            val match = viDatePattern.find(line) ?: continue
            val (day, month, year) = match.destructured
            runCatching {
                val fullYear = if (year.length == 2) ("20$year").toInt() else year.toInt()
                val date = LocalDate.of(fullYear, month.toInt(), day.toInt())
                if (isPlausibleDate(date, now)) return date to true
            }
        }

        // Numeric patterns: dd/MM/yyyy, yyyy-MM-dd, etc.
        for (line in lines) {
            val match = numericDatePattern.find(line) ?: continue
            for (fmt in dateFormats) {
                runCatching {
                    val date = LocalDate.parse(match.value, fmt)
                    if (isPlausibleDate(date, now)) return date to true
                }
            }
        }

        return now to false
    }

    /** Rejects future dates and dates more than 5 years in the past. */
    private fun isPlausibleDate(date: LocalDate, now: LocalDate): Boolean =
        !date.isAfter(now) && date.isAfter(now.minusYears(5))

    // ---- Confidence ----

    private fun computeConfidence(
        merchant: String,
        amount: Double?,
        amountQuality: AmountQuality,
        dateFound: Boolean,
    ): Float {
        var score = 0f

        // How reliably we found the total (max 0.40)
        score += when (amountQuality) {
            AmountQuality.TOTAL -> 0.40f
            AmountQuality.SUBTOTAL -> 0.25f
            AmountQuality.FALLBACK -> 0.10f
            AmountQuality.NONE -> 0f
        }

        // Amount is in a plausible VND range (bonus 0.10)
        if (amount != null && amount >= 1_000) score += 0.10f

        // Date successfully extracted from receipt text (0.25)
        if (dateFound) score += 0.25f

        // Merchant name identified (0.15)
        if (merchant != "Unknown Merchant") score += 0.15f

        return score.coerceIn(0f, 1f)
    }
}
