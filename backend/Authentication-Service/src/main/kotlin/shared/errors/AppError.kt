package com.collektar.shared.errors

import io.ktor.http.*

sealed class AppError(message: String, val statusCode: HttpStatusCode) : Exception(message) {
    sealed class BadRequest(message: String) : AppError(message, HttpStatusCode.BadRequest) {
        data class InvalidUsername(val username: String) : BadRequest(
            "Username must be 3-50 characters, alphanumeric with underscores/hyphens"
        )

        data class InvalidEmail(val email: String) : BadRequest(
            "Invalid email format"
        )

        data class InvalidDisplayName(val displayName: String) : BadRequest(
            "Display name must be 1-100 characters"
        )

        data class InvalidPassword(val reason: String) : BadRequest(
            "Password validation failed: $reason"
        )
    }

    sealed class Unauthorized(message: String) : AppError(message, HttpStatusCode.Unauthorized) {
        class InvalidCredentials : Unauthorized(
            "Invalid Credentials provided."
        )

        class InvalidToken : Unauthorized(
            "Invalid or expired token"
        )
    }

    sealed class Conflict(message: String) : AppError(message, HttpStatusCode.NotFound) {
        data class UsernameTaken(private val username: String) : Conflict(
            "Username $username is already taken"
        )

        data class EmailAlreadyInUse(val email: String) : Conflict(
            "Email is already in use"
        )
    }
}