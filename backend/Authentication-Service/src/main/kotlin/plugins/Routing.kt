package com.collektar.plugins

import com.collektar.dto.ErrorResponse
import com.collektar.features.auth.authRoutes
import com.collektar.features.auth.service.IAuthService
import com.collektar.shared.errors.AppError
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.plugins.statuspages.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
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
                ErrorResponse("Internal Server Error")
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
