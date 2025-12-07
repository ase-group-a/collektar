package com.collektar.shared.security.cookies

import com.collektar.dto.RefreshTokenRequest
import io.ktor.server.application.ApplicationCall

interface ICookieProvider {
    fun set(call: ApplicationCall, name: String, value: String, maxAge: Long, path: String = "/")
    fun delete(call: ApplicationCall, name: String, path: String = "/")
    fun get(call: ApplicationCall, cookieName: String): String
}