package com.collektar.features.auth

import com.collektar.dto.LoginRequest
import com.collektar.dto.RefreshTokenRequest
import com.collektar.dto.RegisterRequest
import com.collektar.features.auth.service.IAuthService
import com.collektar.shared.errors.AppError
import com.collektar.shared.validation.Validator
import io.ktor.http.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Route.authRoutes(authService: IAuthService) {
    val cookieName = "refresh_token"
    val cookiePath = "/"
    val sameSite = mapOf("SameSite" to "Lax")
    val isProd = System.getenv("KTOR_ENVIRONMENT") == "production"

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

        val res = try {
            authService.login(req)
        } catch (e: Exception) {
            call.respond(HttpStatusCode.Unauthorized, mapOf("error" to (e.message ?: "Invalid credentials")))
            return@post
        }

        if (res.refreshToken.isBlank() || res.accessToken.isBlank()) {
            call.respond(HttpStatusCode.InternalServerError, mapOf("error" to "Auth service did not return tokens"))
            return@post
        }

        call.response.cookies.append(
            name = cookieName,
            value = res.refreshToken,
            maxAge = res.refreshTokenExpiresIn,
            path = cookiePath,
            httpOnly = true,
            secure = isProd,
            extensions = sameSite
        )

        call.respond(HttpStatusCode.OK, res)
    }

    post("/refresh") {
        val existing = call.request.cookies[cookieName] ?: throw AppError.Unauthorized.MissingToken()

        val res = try {
            authService.refresh(RefreshTokenRequest(refreshToken = existing))
        } catch (e: Exception) {
            call.respond(HttpStatusCode.Unauthorized, mapOf("error" to (e.message ?: "Invalid refresh token")))
            return@post
        }

        if (res.refreshToken.isBlank() || res.accessToken.isBlank()) {
            call.respond(HttpStatusCode.InternalServerError, mapOf("error" to "Auth service did not return tokens on refresh"))
            return@post
        }

        call.response.cookies.append(
            name = cookieName,
            value = res.refreshToken,
            maxAge = res.refreshTokenExpiresIn,
            path = cookiePath,
            httpOnly = true,
            secure = isProd,
            extensions = sameSite
        )

        call.respond(HttpStatusCode.OK, res)
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
        call.response.cookies.append(
            name = cookieName,
            value = "deleted",
            maxAge = 0,
            path = cookiePath,
            httpOnly = true,
            secure = isProd,
            extensions = sameSite
        )
        call.respond(HttpStatusCode.OK)
    }
}