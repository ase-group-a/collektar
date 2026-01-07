package com.collektar.shared.errors

import io.ktor.http.*

sealed class AppError(message: String, val statusCode: HttpStatusCode) : Exception(message) {
    sealed class BadRequest(message: String) : AppError(message, HttpStatusCode.BadRequest) {
        class InvalidUsername : BadRequest(
            "Username must be 3-50 characters, alphanumeric with underscores/hyphens"
        )

        class InvalidEmail : BadRequest(
            "Invalid email format"
        )

        class InvalidDisplayName : BadRequest(
            "Display name must be 1-100 characters"
        )

        data class InvalidPassword(val reason: String) : BadRequest(
            "Password validation failed: $reason"
        )

        class RefreshTokenMissing : BadRequest(
            "Refresh token is required."
        )

        class InvalidOrExpiredToken : BadRequest(
            "Token is expired or invalid"
        )

        class InvalidUserIdFormat : BadRequest(
            "Invalid user id format"
        )
    }

    sealed class Unauthorized(message: String) : AppError(message, HttpStatusCode.Unauthorized) {
        class InvalidCredentials : Unauthorized(
            "Invalid Credentials provided."
        )

        class InvalidToken : Unauthorized(
            "Invalid or expired token"
        )

        class MissingToken : Unauthorized(
            "Missing authentication token"
        )

        class MissingUserId : Unauthorized(
            "Missing user id"
        )
    }

    sealed class Conflict(message: String) : AppError(message, HttpStatusCode.Conflict) {
        data class UsernameTaken(private val username: String) : Conflict(
            "Username $username is already taken"
        )

        class EmailAlreadyInUse : Conflict(
            "Email is already in use"
        )
    }

    sealed class NotFound(message: String) : AppError(message, HttpStatusCode.NotFound) {
        class UserNotFound : NotFound(
            "Invalid UserId"
        )
    }
}