package com.collektar.features.auth

import com.collektar.dto.LoginRequest
import com.collektar.dto.RefreshTokenRequest
import com.collektar.dto.RegisterRequest
import com.collektar.shared.errors.AppError
import com.collektar.shared.validation.Validator
import io.ktor.http.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Route.authRoutes(authService: IAuthService) {
    route("") {
        post("/register") {
            val req = call.receive<RegisterRequest>()
            Validator.validateUsername(req.username)
            Validator.validateEmail(req.email)
            Validator.validateDisplayName(req.displayName)
            Validator.validatePassword(req.password)
            val res = authService.register(req)
            call.respond(HttpStatusCode.Created, res)
        }

        post("/login") {
            val req = call.receive<LoginRequest>()
            val res = authService.login(req)
            call.respond(HttpStatusCode.OK, res)
        }

        post("/refresh") {
            val req = call.receive<RefreshTokenRequest>()
            if (req.refreshToken.isBlank()) {
                throw AppError.BadRequest.RefreshTokenMissing()
            }
            val res = authService.refresh(req)
            call.respond(HttpStatusCode.OK, res)
        }
    }
}