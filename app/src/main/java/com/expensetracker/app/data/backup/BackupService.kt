package com.expensetracker.app.data.backup

import android.content.Context
import android.net.Uri
import com.expensetracker.app.domain.model.Budget
import com.expensetracker.app.domain.model.Category
import com.expensetracker.app.domain.model.RecurringTransaction
import com.expensetracker.app.domain.model.Transaction
import com.expensetracker.app.domain.model.Wallet
import com.expensetracker.app.domain.repository.BudgetRepository
import com.expensetracker.app.domain.repository.CategoryRepository
import com.expensetracker.app.domain.repository.RecurringTransactionRepository
import com.expensetracker.app.domain.repository.TransactionRepository
import com.expensetracker.app.domain.repository.WalletRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.first
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.time.LocalDate
import java.time.LocalDateTime
import javax.inject.Inject
import javax.inject.Singleton

// ---- Backup DTOs (string-based dates to avoid custom serializers) ----

@Serializable
data class BackupData(
    val version: Int = 1,
    val exportedAt: String = LocalDateTime.now().toString(),
    val wallets: List<WalletDto> = emptyList(),
    val categories: List<CategoryDto> = emptyList(),
    val transactions: List<TransactionDto> = emptyList(),
    val budgets: List<BudgetDto> = emptyList(),
    val recurringTransactions: List<RecurringDto> = emptyList(),
)

@Serializable
data class WalletDto(
    val id: Long, val name: String, val icon: String, val color: String,
    val initialBalance: Double, val currency: String, val createdAt: String,
)

@Serializable
data class CategoryDto(
    val id: Long, val name: String, val icon: String, val color: String,
    val type: String, val isDefault: Boolean, val isArchived: Boolean,
)

@Serializable
data class TransactionDto(
    val id: Long, val walletId: Long, val categoryId: Long, val amount: Double,
    val type: String, val note: String, val date: String, val createdAt: String,
    val updatedAt: String, val photoUri: String?, val location: String?,
    val recurringId: Long?, val parentSplitId: Long?, val tags: List<String>,
    val toWalletId: Long?,
)

@Serializable
data class BudgetDto(
    val id: Long, val categoryId: Long?, val amount: Double, val period: String,
    val startDate: String, val endDate: String?, val alertThreshold: Double, val isActive: Boolean,
)

@Serializable
data class RecurringDto(
    val id: Long, val walletId: Long, val categoryId: Long, val amount: Double,
    val type: String, val note: String, val frequency: String, val interval: Int,
    val startDate: String, val endDate: String?, val nextOccurrence: String,
    val lastProcessed: String?, val isActive: Boolean,
)

@Singleton
class BackupService @Inject constructor(
    @ApplicationContext private val context: Context,
    private val walletRepository: WalletRepository,
    private val categoryRepository: CategoryRepository,
    private val transactionRepository: TransactionRepository,
    private val budgetRepository: BudgetRepository,
    private val recurringRepository: RecurringTransactionRepository,
) {
    private val json = Json { prettyPrint = true; ignoreUnknownKeys = true }

    suspend fun export(uri: Uri): Result<Unit> = runCatching {
        val wallets = walletRepository.observeAll().first()
        val categories = categoryRepository.observeAll().first()
        val transactions = transactionRepository.observeAll().first()
        val budgets = budgetRepository.observeAll().first()
        val recurring = recurringRepository.observeAll().first()

        val backup = BackupData(
            wallets = wallets.map { it.toDto() },
            categories = categories.map { it.toDto() },
            transactions = transactions.map { it.toDto() },
            budgets = budgets.map { it.toDto() },
            recurringTransactions = recurring.map { it.toDto() },
        )

        context.contentResolver.openOutputStream(uri)?.use { stream ->
            stream.write(json.encodeToString(backup).toByteArray())
        } ?: throw IllegalStateException("Cannot open output stream")
    }

    suspend fun import(uri: Uri): Result<BackupData> = runCatching {
        val jsonString = context.contentResolver.openInputStream(uri)?.use { stream ->
            stream.readBytes().toString(Charsets.UTF_8)
        } ?: throw IllegalStateException("Cannot open input stream")
        json.decodeFromString<BackupData>(jsonString)
    }

    suspend fun restoreFrom(backup: BackupData): Result<Unit> = runCatching {
        for (w in backup.wallets) {
            walletRepository.add(
                Wallet(
                    id = 0, name = w.name, icon = w.icon, color = w.color,
                    initialBalance = w.initialBalance, currency = w.currency,
                    createdAt = runCatching { LocalDateTime.parse(w.createdAt) }.getOrElse { LocalDateTime.now() },
                )
            )
        }
        for (c in backup.categories) {
            categoryRepository.add(
                Category(
                    id = 0, name = c.name, icon = c.icon, color = c.color,
                    type = runCatching { com.expensetracker.app.domain.model.TransactionType.valueOf(c.type) }
                        .getOrElse { com.expensetracker.app.domain.model.TransactionType.EXPENSE },
                    isDefault = false, isArchived = false,
                )
            )
        }
    }

    // ---- Mappers ----

    private fun Wallet.toDto() = WalletDto(
        id = id, name = name, icon = icon, color = color,
        initialBalance = initialBalance, currency = currency, createdAt = createdAt.toString(),
    )

    private fun Category.toDto() = CategoryDto(
        id = id, name = name, icon = icon, color = color,
        type = type.name, isDefault = isDefault, isArchived = isArchived,
    )

    private fun Transaction.toDto() = TransactionDto(
        id = id, walletId = walletId, categoryId = categoryId, amount = amount,
        type = type.name, note = note, date = date.toString(),
        createdAt = createdAt.toString(), updatedAt = updatedAt.toString(),
        photoUri = photoUri, location = location, recurringId = recurringId,
        parentSplitId = parentSplitId, tags = tags, toWalletId = toWalletId,
    )

    private fun Budget.toDto() = BudgetDto(
        id = id, categoryId = categoryId, amount = amount, period = period.name,
        startDate = startDate.toString(), endDate = endDate?.toString(),
        alertThreshold = alertThreshold, isActive = isActive,
    )

    private fun RecurringTransaction.toDto() = RecurringDto(
        id = id, walletId = walletId, categoryId = categoryId, amount = amount,
        type = type.name, note = note, frequency = frequency.name, interval = interval,
        startDate = startDate.toString(), endDate = endDate?.toString(),
        nextOccurrence = nextOccurrence.toString(),
        lastProcessed = lastProcessed?.toString(), isActive = isActive,
    )
}
