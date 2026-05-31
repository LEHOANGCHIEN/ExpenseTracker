package com.expensetracker.app.core.util

import java.text.NumberFormat
import java.util.Locale

object CurrencyFormatter {

    fun format(amount: Double, currency: String = "VND"): String {
        return when (currency.uppercase(Locale.ROOT)) {
            "VND" -> {
                val formatter = NumberFormat.getNumberInstance(Locale.forLanguageTag("vi-VN"))
                formatter.isGroupingUsed = true
                formatter.maximumFractionDigits = 0
                "${formatter.format(amount.toLong())} ₫"
            }
            "USD" -> {
                val formatter = NumberFormat.getCurrencyInstance(Locale.US)
                formatter.format(amount)
            }
            "EUR" -> {
                val formatter = NumberFormat.getCurrencyInstance(Locale.forLanguageTag("de-DE"))
                formatter.format(amount)
            }
            else -> {
                val formatter = NumberFormat.getNumberInstance()
                formatter.maximumFractionDigits = 2
                "${formatter.format(amount)} $currency"
            }
        }
    }

    fun formatCompact(amount: Double, currency: String = "VND"): String {
        return when {
            amount >= 1_000_000_000 -> "${format(amount / 1_000_000_000, currency)} tỷ"
            amount >= 1_000_000 -> "${(amount / 1_000_000).toInt()}M"
            amount >= 1_000 -> "${(amount / 1_000).toInt()}K"
            else -> format(amount, currency)
        }
    }
}
