package com.collektar.features.auth

import java.util.*

data class AuthModel(
    val id: UUID,
    val username: String,
    val email: String,
    val displayName: String,
    val passwordHash: String
)
