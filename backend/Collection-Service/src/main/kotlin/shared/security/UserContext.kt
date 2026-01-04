package com.collektar.shared.security

import io.ktor.server.application.ApplicationCall
import io.ktor.server.request.header
import java.util.*

object UserContext {
    fun userIdFromHeader(call: ApplicationCall, headerName: String): UUID? {
        val headerValue = call.request.header(headerName) ?: return null
        return try {
            UUID.fromString(headerValue)
        } catch (e: Exception) {
            null
        }
    }
}
