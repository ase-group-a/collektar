package com.collektar.features.auth.service

import com.collektar.dto.*
import io.ktor.server.routing.*

interface IAuthService {
    suspend fun register(request: RegisterRequest): AuthenticationResponse
    suspend fun login(request: LoginRequest): AuthenticationResponse
    suspend fun refresh(request: RefreshTokenRequest): AuthenticationResponse
    suspend fun verify(token: String, routingCall: RoutingCall)
}