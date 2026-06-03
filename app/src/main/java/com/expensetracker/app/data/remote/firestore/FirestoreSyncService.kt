package com.expensetracker.app.data.remote.firestore

import android.util.Log
import com.expensetracker.app.data.local.CurrentUserProvider
import com.expensetracker.app.data.local.entity.BudgetEntity
import com.expensetracker.app.data.local.entity.CategoryEntity
import com.expensetracker.app.data.local.entity.RecurringTransactionEntity
import com.expensetracker.app.data.local.entity.TransactionEntity
import com.expensetracker.app.data.local.entity.WalletEntity
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.FirebaseFirestore
import javax.inject.Inject
import javax.inject.Singleton

/**
 * One-way push service: mirrors local Room writes up to Firestore under
 * users/{uid}/{collection}/{id}. All operations are fire-and-forget —
 * Firestore's offline persistence queues them locally when the device has
 * no connectivity and syncs automatically when back online. Failures are
 * logged but never surfaced to the user.
 */
@Singleton
class FirestoreSyncService @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val currentUserProvider: CurrentUserProvider,
) {

    // ---- Push (upsert) ----

    fun pushTransaction(entity: TransactionEntity) {
        val uid = currentUserProvider.uid.takeIf { it.isNotEmpty() } ?: return
        userCol(uid, COL_TRANSACTIONS).document(entity.id.toString())
            .set(entity.toFirestoreMap())
            .addOnFailureListener { e -> Log.e(TAG, "push transaction ${entity.id}", e) }
    }

    fun pushWallet(entity: WalletEntity) {
        val uid = currentUserProvider.uid.takeIf { it.isNotEmpty() } ?: return
        userCol(uid, COL_WALLETS).document(entity.id.toString())
            .set(entity.toFirestoreMap())
            .addOnFailureListener { e -> Log.e(TAG, "push wallet ${entity.id}", e) }
    }

    fun pushCategory(entity: CategoryEntity) {
        val uid = currentUserProvider.uid.takeIf { it.isNotEmpty() } ?: return
        userCol(uid, COL_CATEGORIES).document(entity.id.toString())
            .set(entity.toFirestoreMap())
            .addOnFailureListener { e -> Log.e(TAG, "push category ${entity.id}", e) }
    }

    fun pushBudget(entity: BudgetEntity) {
        val uid = currentUserProvider.uid.takeIf { it.isNotEmpty() } ?: return
        userCol(uid, COL_BUDGETS).document(entity.id.toString())
            .set(entity.toFirestoreMap())
            .addOnFailureListener { e -> Log.e(TAG, "push budget ${entity.id}", e) }
    }

    fun pushRecurring(entity: RecurringTransactionEntity) {
        val uid = currentUserProvider.uid.takeIf { it.isNotEmpty() } ?: return
        userCol(uid, COL_RECURRING).document(entity.id.toString())
            .set(entity.toFirestoreMap())
            .addOnFailureListener { e -> Log.e(TAG, "push recurring ${entity.id}", e) }
    }

    // ---- Delete ----

    fun deleteTransaction(id: Long) {
        val uid = currentUserProvider.uid.takeIf { it.isNotEmpty() } ?: return
        userCol(uid, COL_TRANSACTIONS).document(id.toString())
            .delete()
            .addOnFailureListener { e -> Log.e(TAG, "delete transaction $id", e) }
    }

    fun deleteWallet(id: Long) {
        val uid = currentUserProvider.uid.takeIf { it.isNotEmpty() } ?: return
        userCol(uid, COL_WALLETS).document(id.toString())
            .delete()
            .addOnFailureListener { e -> Log.e(TAG, "delete wallet $id", e) }
    }

    fun deleteCategory(id: Long) {
        val uid = currentUserProvider.uid.takeIf { it.isNotEmpty() } ?: return
        userCol(uid, COL_CATEGORIES).document(id.toString())
            .delete()
            .addOnFailureListener { e -> Log.e(TAG, "delete category $id", e) }
    }

    fun deleteBudget(id: Long) {
        val uid = currentUserProvider.uid.takeIf { it.isNotEmpty() } ?: return
        userCol(uid, COL_BUDGETS).document(id.toString())
            .delete()
            .addOnFailureListener { e -> Log.e(TAG, "delete budget $id", e) }
    }

    fun deleteRecurring(id: Long) {
        val uid = currentUserProvider.uid.takeIf { it.isNotEmpty() } ?: return
        userCol(uid, COL_RECURRING).document(id.toString())
            .delete()
            .addOnFailureListener { e -> Log.e(TAG, "delete recurring $id", e) }
    }

    // ---- Helpers ----

    private fun userCol(uid: String, collection: String): CollectionReference =
        firestore.collection("users").document(uid).collection(collection)

    companion object {
        private const val TAG = "FirestoreSync"
        const val COL_TRANSACTIONS = "transactions"
        const val COL_WALLETS = "wallets"
        const val COL_CATEGORIES = "categories"
        const val COL_BUDGETS = "budgets"
        const val COL_RECURRING = "recurring"
    }
}
