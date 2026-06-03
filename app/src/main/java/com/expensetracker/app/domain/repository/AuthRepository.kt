package com.expensetracker.app.domain.repository

import com.expensetracker.app.domain.model.AuthUser
import kotlinx.coroutines.flow.Flow

interface AuthRepository {
    val currentUser: AuthUser?
    fun authStateFlow(): Flow<AuthUser?>
    suspend fun signUp(email: String, password: String): Result<AuthUser>
    suspend fun signIn(email: String, password: String): Result<AuthUser>
    suspend fun signOut()
}
