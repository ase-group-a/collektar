package com.collektar.shared.security.jwt

import com.auth0.jwt.interfaces.DecodedJWT
import java.util.*

interface IJWTService {
    fun generateAccessToken(userId: UUID, email: String): AccessToken
    fun verify(token: String): DecodedJWT?
}