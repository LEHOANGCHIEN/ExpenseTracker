package com.expensetracker.app.feature.auth.viewmodel

import android.content.Context
import android.util.Patterns
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.expensetracker.app.R
import com.expensetracker.app.domain.model.AuthException
import com.expensetracker.app.domain.model.AuthUser
import com.expensetracker.app.domain.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AuthUiState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val resetEmailSent: Boolean = false,
)

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    @ApplicationContext private val context: Context,
) : ViewModel() {

    val authUser: StateFlow<AuthUser?> = authRepository.authStateFlow()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.Eagerly,
            initialValue = authRepository.currentUser,
        )

    private val _uiState = MutableStateFlow(AuthUiState())
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    fun signIn(email: String, password: String) {
        val error = validateInputs(email, password)
        if (error != null) { _uiState.update { it.copy(error = error) }; return }
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            authRepository.signIn(email.trim(), password)
                .onSuccess { _uiState.update { it.copy(isLoading = false) } }
                .onFailure { e -> _uiState.update { it.copy(isLoading = false, error = e.toMessage()) } }
        }
    }

    fun signUp(email: String, password: String) {
        val error = validateInputs(email, password)
        if (error != null) { _uiState.update { it.copy(error = error) }; return }
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            authRepository.signUp(email.trim(), password)
                .onSuccess { _uiState.update { it.copy(isLoading = false) } }
                .onFailure { e -> _uiState.update { it.copy(isLoading = false, error = e.toMessage()) } }
        }
    }

    fun sendPasswordReset(email: String) {
        if (email.isBlank() || !Patterns.EMAIL_ADDRESS.matcher(email.trim()).matches()) {
            _uiState.update { it.copy(error = context.getString(R.string.auth_error_invalid_email_format)) }
            return
        }
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            authRepository.sendPasswordReset(email)
                .onSuccess { _uiState.update { it.copy(isLoading = false, resetEmailSent = true) } }
                .onFailure { e -> _uiState.update { it.copy(isLoading = false, error = e.toMessage()) } }
        }
    }

    fun clearError() = _uiState.update { it.copy(error = null) }

    private fun validateInputs(email: String, password: String): String? = when {
        email.isBlank() -> context.getString(R.string.auth_error_email_required)
        !Patterns.EMAIL_ADDRESS.matcher(email.trim()).matches() -> context.getString(R.string.auth_error_invalid_email_format)
        password.length < 6 -> context.getString(R.string.auth_error_password_too_short)
        else -> null
    }

    private fun Throwable.toMessage(): String = when (val e = this) {
        is AuthException.WeakPassword -> context.getString(R.string.auth_error_weak_password)
        is AuthException.EmailAlreadyInUse -> context.getString(R.string.auth_error_email_in_use)
        is AuthException.WrongPassword -> context.getString(R.string.auth_error_wrong_password)
        is AuthException.InvalidEmail -> context.getString(R.string.auth_error_invalid_email)
        is AuthException.InvalidCredential -> context.getString(R.string.auth_error_invalid_credential)
        is AuthException.UserNotFound -> context.getString(R.string.auth_error_user_not_found)
        is AuthException.UserDisabled -> context.getString(R.string.auth_error_user_disabled)
        is AuthException.NetworkError -> context.getString(R.string.auth_error_network)
        is AuthException.Unknown -> e.message?.takeIf { it.isNotBlank() }
            ?: context.getString(R.string.auth_error_unknown)
        else -> e.message?.takeIf { it.isNotBlank() }
            ?: context.getString(R.string.auth_error_unknown)
    }
}
