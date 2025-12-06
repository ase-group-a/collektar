package com.collektar.shared.security.cookies

import com.collektar.config.AppConfig
import com.collektar.shared.errors.AppError
import io.ktor.server.application.*

class CookieProvider(config: AppConfig): ICookieProvider {
    private val sameSite = mapOf("SameSite" to "Lax")
    private val isProd = config.isProd

    override fun set(call: ApplicationCall, name: String, value: String, maxAge: Long, path: String) {
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

    override fun delete(call: ApplicationCall, name: String, path: String) {
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

    override fun get(call: ApplicationCall, cookieName: String): String {
        return call.request.cookies[cookieName] ?: throw AppError.Unauthorized.MissingToken()
    }
}
