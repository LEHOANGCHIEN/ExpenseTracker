package com.expensetracker.app.data.remote.firestore

import com.expensetracker.app.data.local.entity.BudgetEntity
import com.expensetracker.app.data.local.entity.CategoryEntity
import com.expensetracker.app.data.local.entity.RecurringTransactionEntity
import com.expensetracker.app.data.local.entity.TransactionEntity
import com.expensetracker.app.data.local.entity.WalletEntity

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
