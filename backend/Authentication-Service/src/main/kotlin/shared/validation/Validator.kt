package com.collektar.shared.validation

import com.collektar.shared.errors.AppError

object Validator {
    private val USERNAME_REGEX = Regex("^[a-zA-Z0-9_-]{3,50}$")
    private val EMAIL_REGEX = Regex("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")
    private val passwordRules = listOf(
        "Password must be 8–64 characters" to { p: String -> p.length !in 8..64 },
        "Password cannot contain spaces" to { p: String -> p.contains(Regex("\\s")) },
        "Must contain uppercase letter" to { p: String -> !p.any { it.isUpperCase() } },
        "Must contain lowercase letter" to { p: String -> !p.any { it.isLowerCase() } },
        "Must contain a digit" to { p: String -> !p.any { it.isDigit() } },
        "Must contain special character" to { p: String ->
            !p.any { it in "!@#$%^&*()_+-=[]{}|;:'\",.<>?/" }
        }
    )

    fun validateUsername(username: String) {
        if (!username.matches(USERNAME_REGEX)) {
            throw AppError.BadRequest.InvalidUsername(username)
        }
    }

    fun validateEmail(email: String) {
        if (!email.matches(EMAIL_REGEX)) {
            throw AppError.BadRequest.InvalidEmail(email)
        }
    }

    fun validateDisplayName(displayName: String) {
        if (displayName.isBlank() || displayName.length > 100) {
            throw AppError.BadRequest.InvalidDisplayName(displayName)
        }
    }

    fun validatePassword(password: String) {
        passwordRules
            .firstNotNullOfOrNull { (msg, check) -> msg.takeIf { check(password) } }
            ?.let { throw AppError.BadRequest.InvalidPassword(it) }
    }
}