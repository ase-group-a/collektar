package com.collektar.features.auth

import com.collektar.dto.*
import com.collektar.features.auth.service.IAuthService
import com.collektar.shared.errors.AppError
import com.collektar.shared.security.cookies.ICookieProvider
import com.collektar.shared.utility.userId
import com.collektar.shared.validation.Validator
import io.ktor.http.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Route.authRoutes(authService: IAuthService, cookieProvider: ICookieProvider) {
    post("/register") {
        val req = call.receive<RegisterRequest>()
        Validator.validateUsername(req.username)
        Validator.validateEmail(req.email)
        Validator.validateDisplayName(req.displayName)
        Validator.validatePassword(req.password)
        val res = authService.register(req)
        cookieProvider.set(call, "refresh_token", res.refreshToken, res.refreshTokenExpiresIn)
        call.respond(HttpStatusCode.Created, res.accessTokenResponse)
    }

    post("/login") {
        val req = call.receive<LoginRequest>()
        val res = authService.login(req)

        cookieProvider.set(call, "refresh_token", res.refreshToken, res.refreshTokenExpiresIn)
        call.respond(HttpStatusCode.OK, res.accessTokenResponse)
    }

    post("/refresh") {
        val refreshToken = cookieProvider.get(call, "refresh_token")
        val req = RefreshTokenRequest(refreshToken)

        if (req.refreshToken.isBlank()) {
            throw AppError.BadRequest.RefreshTokenMissing()
        }
        val res = authService.refresh(req)

        cookieProvider.set(call, "refresh_token", res.refreshToken, res.refreshTokenExpiresIn)
        call.respond(HttpStatusCode.OK, res.accessTokenResponse)
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
        authService.logout(cookieProvider.get(call, "refresh_token"))
        cookieProvider.delete(call, "refresh_token")
        call.respond(HttpStatusCode.OK)
    }

    post("/forgot-password") {
        val req = call.receive<ForgotPasswordRequest>()
        Validator.validateEmail(req.email)
        authService.forgotPassword(req)

        call.respond(HttpStatusCode.OK)
    }

    post("/reset-password") {
        val req = call.receive<ResetPasswordRequest>()
        Validator.validatePassword(req.newPassword)
        authService.resetPassword(req)
        call.respond(HttpStatusCode.OK)
    }

    post("/change-password") {
        val userId = call.userId
        val req = call.receive<ChangePasswordRequest>()
        Validator.validatePassword(req.newPassword)
        authService.changePassword(userId, req)
        cookieProvider.delete(call, "refresh_token")
        call.respond(HttpStatusCode.OK)
    }

    delete("/account") {
        val userId = call.userId
        val req = call.receive<DeleteAccountRequest>()
        authService.deleteAccount(userId, req)
        cookieProvider.delete(call, "refresh_token")
        call.respond(HttpStatusCode.OK)
    }
}