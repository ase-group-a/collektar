package com.collektar.dto

import kotlinx.serialization.Serializable

@Serializable
data class RegisterRequest(
    val email: String,
    val username: String,
    val displayName: String,
    val password: String
)

@Serializable
data class UserInfo(
    val email: String,
    val username: String,
    val displayName: String
)

@Serializable
data class LoginRequest(
    val username: String,
    val password: String
)

@Serializable
data class AuthenticationResponse(
    val accessToken: String,
    val tokenType: String = "Bearer",
    val expiresIn: Long,
    val refreshToken: String,
    val refreshTokenExpiresIn: Long,
    val user: UserInfo
)

@Serializable
data class RefreshTokenRequest(
    val refreshToken: String
)