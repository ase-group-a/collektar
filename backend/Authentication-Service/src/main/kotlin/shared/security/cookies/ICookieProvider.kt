package com.collektar.shared.security.cookies

import io.ktor.server.application.ApplicationCall

interface ICookieProvider {
    fun set(call: ApplicationCall, name: String, value: String, maxAge: Long, path: String = "/")
    fun delete(call: ApplicationCall, name: String, path: String = "/")
}
