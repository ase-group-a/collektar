package com.collektar.shared.security.cookies

import io.ktor.server.application.*

class CookieProvider(private val isProd: Boolean) {
    private val sameSite = mapOf("SameSite" to "Lax")

    fun set(call: ApplicationCall, name: String, value: String, maxAge: Long, path: String = "/") {
        call.response.cookies.append(
            name = name,
            value = value,
            maxAge = maxAge,
            path = path,
            httpOnly = true,
            secure = isProd,
            extensions = sameSite
        )
    }

    fun delete(call: ApplicationCall, name: String, path: String = "/") {
        call.response.cookies.append(
            name = name,
            value = "deleted",
            maxAge = 0,
            path = path,
            httpOnly = true,
            secure = isProd,
            extensions = sameSite
        )
    }
}
