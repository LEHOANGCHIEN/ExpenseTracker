package com.expensetracker.app.data.remote.firestore

import android.util.Log
import com.expensetracker.app.data.local.dao.BudgetDao
import com.expensetracker.app.data.local.dao.CategoryDao
import com.expensetracker.app.data.local.dao.RecurringTransactionDao
import com.expensetracker.app.data.local.dao.TransactionDao
import com.expensetracker.app.data.local.dao.WalletDao
import com.expensetracker.app.data.local.entity.CategoryEntity
import com.expensetracker.app.data.local.entity.WalletEntity
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Pulls all user data from Firestore into Room on sign-in or app start.
 * Intentionally does NOT inject CurrentUserProvider — uid is passed explicitly
 * so this class has no dependency on AuthRepository and avoids the DI cycle:
 *   AuthRepositoryImpl → UserBootstrapService → FirestoreCloudSyncService (safe)
 *   vs.
 *   AuthRepositoryImpl → UserBootstrapService → FirestoreSyncService → CurrentUserProvider
 *     → AuthRepository → AuthRepositoryImpl  (circular — rejected by Hilt)
 */
@Singleton
class FirestoreCloudSyncService @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val walletDao: WalletDao,
    private val categoryDao: CategoryDao,
    private val transactionDao: TransactionDao,
    private val budgetDao: BudgetDao,
    private val recurringDao: RecurringTransactionDao,
) {

    /**
     * Fetches every collection for [uid] from Firestore and upserts into Room.
     * Insertion order respects FK dependencies: wallets → categories → recurring → budgets → transactions.
     * Each step is logged; any exception is caught and logged with full stacktrace so callers always complete.
     */
    @Suppress("UNCHECKED_CAST")
    suspend fun syncFromCloud(uid: String) {
        Log.d(TAG, "syncFromCloud START uid=$uid")
        if (uid.isEmpty()) {
            Log.w(TAG, "syncFromCloud ABORT: uid is empty")
            return
        }

        try {
            val userDoc = firestore.collection("users").document(uid)

            // 1. Wallets
            val walletSnap = userDoc.collection(COL_WALLETS).get().await()
            Log.d(TAG, "syncFromCloud wallets fetched: ${walletSnap.size()} docs")
            var walletOk = 0
            for (doc in walletSnap) {
                val data = doc.data as Map<String, Any?>
                val entity = data.toWalletEntityOrNull()
                if (entity != null) { walletDao.insert(entity); walletOk++ }
                else Log.w(TAG, "syncFromCloud wallet doc ${doc.id} could not be mapped")
            }
            Log.d(TAG, "syncFromCloud wallets written to Room: $walletOk/${walletSnap.size()}")

            // 2. Categories
            val catSnap = userDoc.collection(COL_CATEGORIES).get().await()
            Log.d(TAG, "syncFromCloud categories fetched: ${catSnap.size()} docs")
            var catOk = 0
            for (doc in catSnap) {
                val data = doc.data as Map<String, Any?>
                val entity = data.toCategoryEntityOrNull()
                if (entity != null) { categoryDao.insert(entity); catOk++ }
                else Log.w(TAG, "syncFromCloud category doc ${doc.id} could not be mapped")
            }
            Log.d(TAG, "syncFromCloud categories written to Room: $catOk/${catSnap.size()}")

            // 3. Recurring transactions (before transactions, after wallets+categories)
            val recurSnap = userDoc.collection(COL_RECURRING).get().await()
            Log.d(TAG, "syncFromCloud recurring fetched: ${recurSnap.size()} docs")
            var recurOk = 0
            for (doc in recurSnap) {
                val data = doc.data as Map<String, Any?>
                val entity = data.toRecurringEntityOrNull()
                if (entity != null) { recurringDao.insert(entity); recurOk++ }
                else Log.w(TAG, "syncFromCloud recurring doc ${doc.id} could not be mapped")
            }
            Log.d(TAG, "syncFromCloud recurring written to Room: $recurOk/${recurSnap.size()}")

            // 4. Budgets
            val budgetSnap = userDoc.collection(COL_BUDGETS).get().await()
            Log.d(TAG, "syncFromCloud budgets fetched: ${budgetSnap.size()} docs")
            var budgetOk = 0
            for (doc in budgetSnap) {
                val data = doc.data as Map<String, Any?>
                val entity = data.toBudgetEntityOrNull()
                if (entity != null) { budgetDao.insert(entity); budgetOk++ }
                else Log.w(TAG, "syncFromCloud budget doc ${doc.id} could not be mapped")
            }
            Log.d(TAG, "syncFromCloud budgets written to Room: $budgetOk/${budgetSnap.size()}")

            // 5. Transactions — last because they reference wallets, categories, recurring.
            // Sort parents before split children to respect the self-referential FK.
            val txSnap = userDoc.collection(COL_TRANSACTIONS).get().await()
            Log.d(TAG, "syncFromCloud transactions fetched: ${txSnap.size()} docs")
            val allTx = txSnap.mapNotNull { doc ->
                val data = doc.data as Map<String, Any?>
                data.toTransactionEntityOrNull().also { entity ->
                    if (entity == null) Log.w(TAG, "syncFromCloud transaction doc ${doc.id} could not be mapped")
                }
            }
            val parents = allTx.filter { it.parentSplitId == null }
            val children = allTx.filter { it.parentSplitId != null }
            transactionDao.insertAll(parents)
            transactionDao.insertAll(children)
            Log.d(TAG, "syncFromCloud transactions written to Room: ${allTx.size}/${txSnap.size()} (parents=${parents.size} children=${children.size})")

            Log.d(TAG, "syncFromCloud DONE uid=$uid")
        } catch (e: Exception) {
            Log.e(TAG, "syncFromCloud FAILED uid=$uid", e)
        }
    }

    /**
     * Fire-and-forget push for entities that were seeded as defaults for a brand-new account.
     * Ensures defaults land in Firestore so a second device can pull them instead of re-seeding.
     */
    fun pushSeedData(uid: String, wallets: List<WalletEntity>, categories: List<CategoryEntity>) {
        if (uid.isEmpty()) return
        val userDoc = firestore.collection("users").document(uid)
        Log.d(TAG, "pushSeedData START uid=$uid wallets=${wallets.size} categories=${categories.size}")
        wallets.forEach { w ->
            userDoc.collection(COL_WALLETS).document(w.id.toString())
                .set(w.toFirestoreMap())
                .addOnFailureListener { e -> Log.e(TAG, "pushSeedData wallet ${w.id} failed", e) }
        }
        categories.forEach { c ->
            userDoc.collection(COL_CATEGORIES).document(c.id.toString())
                .set(c.toFirestoreMap())
                .addOnFailureListener { e -> Log.e(TAG, "pushSeedData category ${c.id} failed", e) }
        }
        Log.d(TAG, "pushSeedData enqueued uid=$uid")
    }

    companion object {
        private const val TAG = "FirestoreSync"
        private const val COL_TRANSACTIONS = "transactions"
        private const val COL_WALLETS = "wallets"
        private const val COL_CATEGORIES = "categories"
        private const val COL_BUDGETS = "budgets"
        private const val COL_RECURRING = "recurring"
    }
}
