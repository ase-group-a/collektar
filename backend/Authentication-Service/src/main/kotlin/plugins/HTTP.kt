package com.collektar.plugins

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.plugins.cors.routing.*
import io.ktor.server.plugins.swagger.*
import io.ktor.server.routing.*

fun Application.configureHTTP() {
    routing {
        swaggerUI(path = "openapi")
    }

    val isProd = System.getenv("KTOR_ENVIRONMENT") == "production"
    val domain = System.getenv("DOMAIN") ?: "localhost"

    install(CORS) {
        allowMethod(HttpMethod.Options)
        allowMethod(HttpMethod.Put)
        allowMethod(HttpMethod.Delete)
        allowMethod(HttpMethod.Patch)
        allowMethod(HttpMethod.Post)
        allowMethod(HttpMethod.Get)
        allowHeader(HttpHeaders.ContentType)
        allowCredentials = true
        allowHeader(HttpHeaders.Authorization)
        if (isProd) {
            allowHost(domain, schemes = listOf("https"))
        } else {
            anyHost()
        }
    }
}
