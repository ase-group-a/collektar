package com.collektar

import com.collektar.DTOs.ErrorResponse
import com.collektar.features.auth.IAuthService
import com.collektar.features.auth.authRoutes
import com.collektar.shared.errors.AppError
import io.ktor.client.*
import io.ktor.client.engine.apache.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.metrics.micrometer.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.plugins.cors.routing.*
import io.ktor.server.plugins.statuspages.*
import io.ktor.server.plugins.swagger.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.sessions.*
import io.micrometer.prometheus.*
import org.jetbrains.exposed.sql.*
import org.koin.ktor.ext.get

fun Application.configureRouting() {
    val authService = get<IAuthService>()

    install(StatusPages) {
        exception<AppError> { call, cause ->
            call.respond(
                cause.statusCode,
                ErrorResponse(message = cause.message ?: "Unexpected error occurred.")
            )
        }
        exception<Throwable> { call, cause ->
            call.application.log.error("Unhandled exception caught", cause)
            call.respond(
                HttpStatusCode.InternalServerError,
                ErrorResponse(message = "Internal Server Error: $cause")
            )
        }
    }

    routing {
        get("/hello") {
            call.respondText("Hello from Authentication Service")
        }

        get("/health") {
            call.respondText("OK", ContentType.Text.Plain, HttpStatusCode.OK)
        }
        authRoutes(authService)
    }
}
