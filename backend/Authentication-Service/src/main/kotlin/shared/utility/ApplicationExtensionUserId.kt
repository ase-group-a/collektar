package com.collektar.shared.utility

import com.collektar.shared.errors.AppError
import io.ktor.server.application.*
import java.util.*

val ApplicationCall.userId: UUID
    get() {
        val userIdString = request.headers["X-User-Id"]
            ?: throw AppError.Unauthorized.MissingUserId()

        try {
            return UUID.fromString(userIdString)
        } catch (_: IllegalArgumentException) {
            throw AppError.BadRequest.InvalidUserIdFormat()
        }
    }