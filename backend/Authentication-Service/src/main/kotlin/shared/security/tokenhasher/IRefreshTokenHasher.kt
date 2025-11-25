package com.collektar.shared.security.tokenhasher

interface IRefreshTokenHasher {
    fun hash(refreshToken: String): String
}