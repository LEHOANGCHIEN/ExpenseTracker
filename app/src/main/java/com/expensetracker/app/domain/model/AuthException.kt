package com.expensetracker.app.domain.model

sealed class AuthException(message: String = "") : Exception(message) {
    class WeakPassword : AuthException()
    class EmailAlreadyInUse : AuthException()
    class WrongPassword : AuthException()
    class InvalidEmail : AuthException()
    class InvalidCredential : AuthException()
    class UserNotFound : AuthException()
    class UserDisabled : AuthException()
    class NetworkError : AuthException()
    class Unknown(msg: String = "") : AuthException(msg)
}
