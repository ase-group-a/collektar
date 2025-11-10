package com.collektar.features.auth

import com.collektar.dto.*
import java.util.*

interface IAuthService {
    suspend fun register(request: RegisterRequest): RegisterResponse
    suspend fun login(request: LoginRequest): AuthenticationResponse
    suspend fun refresh(request: RefreshTokenRequest): AuthenticationResponse
}