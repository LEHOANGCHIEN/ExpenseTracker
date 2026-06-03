package com.expensetracker.app.data.repository

import com.expensetracker.app.data.bootstrap.UserBootstrapService
import com.expensetracker.app.domain.model.AuthUser
import com.expensetracker.app.domain.repository.AuthRepository
import com.google.firebase.FirebaseNetworkException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.FirebaseAuthWeakPasswordException
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthRepositoryImpl @Inject constructor(
    private val firebaseAuth: FirebaseAuth,
    private val userBootstrapService: UserBootstrapService,
) : AuthRepository {

    override val currentUser: AuthUser?
        get() = firebaseAuth.currentUser?.toAuthUser()

    override fun authStateFlow(): Flow<AuthUser?> = callbackFlow {
        val listener = FirebaseAuth.AuthStateListener { auth ->
            trySend(auth.currentUser?.toAuthUser())
        }
        firebaseAuth.addAuthStateListener(listener)
        awaitClose { firebaseAuth.removeAuthStateListener(listener) }
    }

    override suspend fun signUp(email: String, password: String): Result<AuthUser> =
        try {
            val result = firebaseAuth.createUserWithEmailAndPassword(email, password).await()
            val user = result.user ?: return Result.failure(Exception("Sign-up failed"))
            val authUser = user.toAuthUser()
            userBootstrapService.bootstrapIfNeeded(authUser.uid)
            Result.success(authUser)
        } catch (e: Exception) {
            Result.failure(Exception(e.toAuthMessage()))
        }

    override suspend fun signIn(email: String, password: String): Result<AuthUser> =
        try {
            val result = firebaseAuth.signInWithEmailAndPassword(email, password).await()
            val user = result.user ?: return Result.failure(Exception("Sign-in failed"))
            val authUser = user.toAuthUser()
            userBootstrapService.bootstrapIfNeeded(authUser.uid)
            Result.success(authUser)
        } catch (e: Exception) {
            Result.failure(Exception(e.toAuthMessage()))
        }

    override suspend fun signOut() {
        firebaseAuth.signOut()
    }
}

private fun FirebaseUser.toAuthUser() = AuthUser(uid = uid, email = email ?: "")

private fun Exception.toAuthMessage(): String = when (this) {
    is FirebaseAuthWeakPasswordException -> "Password is too weak"
    is FirebaseAuthUserCollisionException -> "An account already exists with this email"
    is FirebaseAuthInvalidCredentialsException -> when (errorCode) {
        "ERROR_WRONG_PASSWORD" -> "Wrong password"
        "ERROR_INVALID_EMAIL" -> "Invalid email address"
        "ERROR_INVALID_CREDENTIAL" -> "Invalid email or password"
        else -> "Invalid credentials"
    }
    is FirebaseAuthInvalidUserException -> when (errorCode) {
        "ERROR_USER_NOT_FOUND" -> "No account found with this email"
        "ERROR_USER_DISABLED" -> "This account has been disabled"
        else -> message ?: "Authentication error"
    }
    is FirebaseNetworkException -> "Network error. Check your connection"
    else -> message ?: "Authentication error"
}
