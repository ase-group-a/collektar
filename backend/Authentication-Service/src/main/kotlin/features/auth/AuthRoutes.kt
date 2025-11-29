package com.collektar.features.auth

import com.collektar.dto.LoginRequest
import com.collektar.dto.RefreshTokenRequest
import com.collektar.dto.RegisterRequest
import com.collektar.features.auth.service.IAuthService
import com.collektar.shared.errors.AppError
import com.collektar.shared.security.cookies.CookieProvider
import com.collektar.shared.validation.Validator
import io.ktor.http.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Route.authRoutes(authService: IAuthService, cookieProvider: CookieProvider) {
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

        cookieProvider.set(call, "refresh_token", res.refreshToken, res.refreshTokenExpiresIn)
        call.respond(HttpStatusCode.OK, mapOf("accessToken" to res.accessToken, "expiresIn" to res.expiresIn, "user" to res.user))
    }

    post("/refresh") {
        val req = call.receive<RefreshTokenRequest>()
        if (req.refreshToken.isBlank()) {
            throw AppError.BadRequest.RefreshTokenMissing()
        }
        val res = authService.refresh(req)

        cookieProvider.set(call, "refresh_token", res.refreshToken, res.refreshTokenExpiresIn)
        call.respond(HttpStatusCode.OK, mapOf("accessToken" to res.accessToken, "expiresIn" to res.expiresIn, "user" to res.user))
    }

    get("/verify") {
        val authHeader = call.request.header(
            "Authorization"
        ) ?: throw AppError.Unauthorized.MissingToken()
        if (!authHeader.startsWith("Bearer ")) {
            throw AppError.Unauthorized.InvalidToken()
        }
        val token = authHeader.removePrefix("Bearer ")
        if (token.isBlank()) {
            throw AppError.Unauthorized.MissingToken()
        }
        authService.verify(token, call)
        call.respond(HttpStatusCode.OK)
    }

    post("/logout") {
        cookieProvider.delete(call, "refresh_token")
        call.respond(HttpStatusCode.OK)
    }
}