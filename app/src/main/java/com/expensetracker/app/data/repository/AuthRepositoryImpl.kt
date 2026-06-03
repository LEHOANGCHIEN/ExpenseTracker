package com.expensetracker.app.data.repository

import com.expensetracker.app.data.bootstrap.UserBootstrapService
import com.expensetracker.app.domain.model.AuthException
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
            val user = result.user ?: return Result.failure(AuthException.Unknown())
            val authUser = user.toAuthUser()
            userBootstrapService.bootstrapIfNeeded(authUser.uid)
            Result.success(authUser)
        } catch (e: Exception) {
            Result.failure(e.toAuthException())
        }

    override suspend fun signIn(email: String, password: String): Result<AuthUser> =
        try {
            val result = firebaseAuth.signInWithEmailAndPassword(email, password).await()
            val user = result.user ?: return Result.failure(AuthException.Unknown())
            val authUser = user.toAuthUser()
            userBootstrapService.bootstrapIfNeeded(authUser.uid)
            Result.success(authUser)
        } catch (e: Exception) {
            Result.failure(e.toAuthException())
        }

    override suspend fun signOut() {
        firebaseAuth.signOut()
    }

    override suspend fun sendPasswordReset(email: String): Result<Unit> =
        try {
            firebaseAuth.sendPasswordResetEmail(email.trim()).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e.toAuthException())
        }
}

private fun FirebaseUser.toAuthUser() = AuthUser(uid = uid, email = email ?: "")

private fun Exception.toAuthException(): AuthException = when (this) {
    is AuthException -> this
    is FirebaseAuthWeakPasswordException -> AuthException.WeakPassword()
    is FirebaseAuthUserCollisionException -> AuthException.EmailAlreadyInUse()
    is FirebaseAuthInvalidCredentialsException -> when (errorCode) {
        "ERROR_WRONG_PASSWORD" -> AuthException.WrongPassword()
        "ERROR_INVALID_EMAIL" -> AuthException.InvalidEmail()
        else -> AuthException.InvalidCredential()
    }
    is FirebaseAuthInvalidUserException -> when (errorCode) {
        "ERROR_USER_NOT_FOUND" -> AuthException.UserNotFound()
        "ERROR_USER_DISABLED" -> AuthException.UserDisabled()
        else -> AuthException.Unknown(message ?: "")
    }
    is FirebaseNetworkException -> AuthException.NetworkError()
    else -> AuthException.Unknown(message ?: "")
}
