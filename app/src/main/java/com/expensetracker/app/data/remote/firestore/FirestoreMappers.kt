package com.expensetracker.app.data.remote.firestore

import android.util.Log
import com.expensetracker.app.data.local.entity.BudgetEntity
import com.expensetracker.app.data.local.entity.CategoryEntity
import com.expensetracker.app.data.local.entity.RecurringTransactionEntity
import com.expensetracker.app.data.local.entity.TransactionEntity
import com.expensetracker.app.data.local.entity.WalletEntity
import com.expensetracker.app.domain.model.BudgetPeriod
import com.expensetracker.app.domain.model.RecurrenceFrequency
import com.expensetracker.app.domain.model.TransactionType
import java.time.LocalDate
import java.time.LocalDateTime

private const val TAG_MAPPER = "FirestoreSync"

// Each mapper captures a syncedAt epoch-millis timestamp so documents carry a
// last-write-wins field usable for conflict resolution in a future pull stage.

internal fun TransactionEntity.toFirestoreMap(): Map<String, Any?> = mapOf(
    "id" to id,
    "userId" to userId,
    "walletId" to walletId,
    "categoryId" to categoryId,
    "amount" to amount,
    "type" to type.name,
    "note" to note,
    "date" to date.toString(),               // ISO-8601 e.g. "2024-06-03"
    "createdAt" to createdAt.toString(),     // ISO-8601 LocalDateTime
    "updatedAt" to updatedAt.toString(),     // ISO-8601 LocalDateTime (entity field)
    "photoUri" to photoUri,
    "location" to location,
    "recurringId" to recurringId,
    "parentSplitId" to parentSplitId,
    "tags" to tags,                          // List<String> → Firestore array
    "toWalletId" to toWalletId,
    "syncedAt" to System.currentTimeMillis(), // epoch millis at push time
)

internal fun WalletEntity.toFirestoreMap(): Map<String, Any?> = mapOf(
    "id" to id,
    "userId" to userId,
    "name" to name,
    "icon" to icon,
    "color" to color,
    "initialBalance" to initialBalance,
    "currency" to currency,
    "createdAt" to createdAt.toString(),
    "syncedAt" to System.currentTimeMillis(),
)

internal fun CategoryEntity.toFirestoreMap(): Map<String, Any?> = mapOf(
    "id" to id,
    "userId" to userId,
    "name" to name,
    "icon" to icon,
    "color" to color,
    "type" to type.name,
    "isDefault" to isDefault,
    "isArchived" to isArchived,
    "syncedAt" to System.currentTimeMillis(),
)

internal fun BudgetEntity.toFirestoreMap(): Map<String, Any?> = mapOf(
    "id" to id,
    "userId" to userId,
    "categoryId" to categoryId,
    "amount" to amount,
    "period" to period.name,
    "startDate" to startDate.toString(),
    "endDate" to endDate?.toString(),
    "alertThreshold" to alertThreshold,
    "isActive" to isActive,
    "syncedAt" to System.currentTimeMillis(),
)

internal fun RecurringTransactionEntity.toFirestoreMap(): Map<String, Any?> = mapOf(
    "id" to id,
    "userId" to userId,
    "walletId" to walletId,
    "categoryId" to categoryId,
    "amount" to amount,
    "type" to type.name,
    "note" to note,
    "frequency" to frequency.name,
    "interval" to interval,
    "startDate" to startDate.toString(),
    "endDate" to endDate?.toString(),
    "nextOccurrence" to nextOccurrence.toString(),
    "lastProcessed" to lastProcessed?.toString(),
    "isActive" to isActive,
    "syncedAt" to System.currentTimeMillis(),
)

// ---- Reverse mappers: Firestore document data → Room entity ----
// Used during cloud pull. Each function returns null and logs if a required field
// is missing or unparseable so bad documents are skipped rather than crash.

private fun Any?.asLong(): Long? = when (this) {
    is Long -> this
    is Int -> this.toLong()
    is Double -> this.toLong()
    else -> null
}

private fun Any?.asDouble(): Double? = when (this) {
    is Double -> this
    is Long -> this.toDouble()
    is Int -> this.toDouble()
    else -> null
}

private fun Any?.asString(): String? = this as? String

private fun Any?.asBoolean(): Boolean? = this as? Boolean

private fun Any?.asStringList(): List<String> =
    (this as? List<*>)?.filterIsInstance<String>() ?: emptyList()

internal fun Map<String, Any?>.toTransactionEntityOrNull(): TransactionEntity? {
    return try {
        TransactionEntity(
            id = get("id").asLong() ?: return null,
            userId = get("userId").asString() ?: "",
            walletId = get("walletId").asLong() ?: return null,
            categoryId = get("categoryId").asLong() ?: return null,
            amount = get("amount").asDouble() ?: return null,
            type = TransactionType.valueOf(get("type").asString() ?: return null),
            note = get("note").asString() ?: "",
            date = LocalDate.parse(get("date").asString() ?: return null),
            createdAt = LocalDateTime.parse(get("createdAt").asString() ?: return null),
            updatedAt = LocalDateTime.parse(get("updatedAt").asString() ?: return null),
            photoUri = get("photoUri").asString(),
            location = get("location").asString(),
            recurringId = get("recurringId").asLong(),
            parentSplitId = get("parentSplitId").asLong(),
            tags = get("tags").asStringList(),
            toWalletId = get("toWalletId").asLong(),
        )
    } catch (e: Exception) {
        Log.e(TAG_MAPPER, "toTransactionEntityOrNull: mapping failed", e)
        null
    }
}

internal fun Map<String, Any?>.toWalletEntityOrNull(): WalletEntity? {
    return try {
        WalletEntity(
            id = get("id").asLong() ?: return null,
            userId = get("userId").asString() ?: "",
            name = get("name").asString() ?: return null,
            icon = get("icon").asString() ?: "",
            color = get("color").asString() ?: "",
            initialBalance = get("initialBalance").asDouble() ?: 0.0,
            currency = get("currency").asString() ?: "VND",
            createdAt = LocalDateTime.parse(get("createdAt").asString() ?: return null),
        )
    } catch (e: Exception) {
        Log.e(TAG_MAPPER, "toWalletEntityOrNull: mapping failed", e)
        null
    }
}

internal fun Map<String, Any?>.toCategoryEntityOrNull(): CategoryEntity? {
    return try {
        CategoryEntity(
            id = get("id").asLong() ?: return null,
            userId = get("userId").asString() ?: "",
            name = get("name").asString() ?: return null,
            icon = get("icon").asString() ?: "",
            color = get("color").asString() ?: "",
            type = TransactionType.valueOf(get("type").asString() ?: return null),
            isDefault = get("isDefault").asBoolean() ?: false,
            isArchived = get("isArchived").asBoolean() ?: false,
        )
    } catch (e: Exception) {
        Log.e(TAG_MAPPER, "toCategoryEntityOrNull: mapping failed", e)
        null
    }
}

internal fun Map<String, Any?>.toBudgetEntityOrNull(): BudgetEntity? {
    return try {
        BudgetEntity(
            id = get("id").asLong() ?: return null,
            userId = get("userId").asString() ?: "",
            categoryId = get("categoryId").asLong(),
            amount = get("amount").asDouble() ?: return null,
            period = BudgetPeriod.valueOf(get("period").asString() ?: return null),
            startDate = LocalDate.parse(get("startDate").asString() ?: return null),
            endDate = get("endDate").asString()?.let { LocalDate.parse(it) },
            alertThreshold = get("alertThreshold").asDouble() ?: 0.0,
            isActive = get("isActive").asBoolean() ?: true,
        )
    } catch (e: Exception) {
        Log.e(TAG_MAPPER, "toBudgetEntityOrNull: mapping failed", e)
        null
    }
}

internal fun Map<String, Any?>.toRecurringEntityOrNull(): RecurringTransactionEntity? {
    return try {
        RecurringTransactionEntity(
            id = get("id").asLong() ?: return null,
            userId = get("userId").asString() ?: "",
            walletId = get("walletId").asLong() ?: return null,
            categoryId = get("categoryId").asLong() ?: return null,
            amount = get("amount").asDouble() ?: return null,
            type = TransactionType.valueOf(get("type").asString() ?: return null),
            note = get("note").asString() ?: "",
            frequency = RecurrenceFrequency.valueOf(get("frequency").asString() ?: return null),
            interval = get("interval").asLong()?.toInt() ?: return null,
            startDate = LocalDate.parse(get("startDate").asString() ?: return null),
            endDate = get("endDate").asString()?.let { LocalDate.parse(it) },
            nextOccurrence = LocalDate.parse(get("nextOccurrence").asString() ?: return null),
            lastProcessed = get("lastProcessed").asString()?.let { LocalDate.parse(it) },
            isActive = get("isActive").asBoolean() ?: true,
        )
    } catch (e: Exception) {
        Log.e(TAG_MAPPER, "toRecurringEntityOrNull: mapping failed", e)
        null
    }
}
