package com.expensetracker.app.data.remote.gemini

import java.time.LocalDate

object GeminiPrompts {

    // ---- Chat / Financial Advisor ----

    fun buildChatSystemPrompt(userContextJson: String): String = """
You are a personal financial advisor inside an expense tracker app.
The user's data is provided below as JSON. Use it to give personalized, concrete, and actionable advice.
Be concise (2-4 sentences typically), friendly, and non-judgmental.
If you don't know, say so. If a number is involved, format it with the user's currency.
User context: $userContextJson
    """.trimIndent()

    // ---- Natural Language Transaction Parsing ----

    fun buildNlParsePrompt(
        text: String,
        categoriesList: String,
        currency: String,
    ): String = """
Parse the user's text into a transaction. Available categories: $categoriesList.
Today's date is ${LocalDate.now()}. The user's currency is $currency.
Return ONLY a JSON object with this schema:
{
  "amount": number,
  "type": "EXPENSE" | "INCOME",
  "categoryId": number,
  "note": string,
  "date": "YYYY-MM-DD",
  "confidence": number between 0 and 1
}
If you can't parse, return {"error": "reason"}.
User text: "$text"
    """.trimIndent()

    // ---- Auto-Categorization ----

    fun buildCategorizationPrompt(
        note: String,
        amount: Double,
        categoriesList: String,
    ): String = """
Suggest the best category for this transaction. Categories: $categoriesList.
Note: "$note", Amount: $amount.
Return ONLY: {"categoryId": number, "confidence": number}.
    """.trimIndent()

    // ---- Monthly Insights ----

    fun buildMonthlyInsightsPrompt(
        month: String,
        transactionsSummaryJson: String,
        previousMonthJson: String,
    ): String = """
Analyze the user's spending for $month. Data: $transactionsSummaryJson.
Compare to previous month: $previousMonthJson.
Generate 2-4 insights in JSON array form:
[{"type": "MONTHLY_SUMMARY" | "ANOMALY" | "RECOMMENDATION", "title": string, "content": string}]
Be specific, use numbers, focus on actionable observations.
    """.trimIndent()

    // ---- Receipt OCR Fallback ----

    fun buildReceiptParsePrompt(rawOcrText: String): String = """
Parse this OCR'd receipt into structured data.
Return JSON: {"amount": number, "date": "YYYY-MM-DD", "merchant": string, "currency": string}.
If a field can't be determined, use null.
Text:
$rawOcrText
    """.trimIndent()
}
