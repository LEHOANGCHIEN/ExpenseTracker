package com.expensetracker.app.data.local

import com.expensetracker.app.domain.repository.AuthRepository
import javax.inject.Inject
import javax.inject.Singleton

interface CurrentUserProvider {
    val uid: String
}

@Singleton
class CurrentUserProviderImpl @Inject constructor(
    private val authRepository: AuthRepository,
) : CurrentUserProvider {
    override val uid: String
        get() = authRepository.currentUser?.uid ?: ""
}
