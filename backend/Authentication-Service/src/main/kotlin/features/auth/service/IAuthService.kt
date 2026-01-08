package com.collektar.features.auth.service

import com.collektar.dto.*
import io.ktor.server.routing.*
import java.util.*

interface IAuthService {
    suspend fun register(request: RegisterRequest): AuthenticationResponse
    suspend fun login(request: LoginRequest): AuthenticationResponse
    suspend fun refresh(request: RefreshTokenRequest): AuthenticationResponse
    suspend fun verify(token: String, routingCall: RoutingCall)
    suspend fun logout(token: String)
    suspend fun forgotPassword(request: ForgotPasswordRequest)
    suspend fun resetPassword(request: ResetPasswordRequest)
    suspend fun changePassword(userId: UUID, request: ChangePasswordRequest)
    suspend fun deleteAccount(userId: UUID, request: DeleteAccountRequest)
}