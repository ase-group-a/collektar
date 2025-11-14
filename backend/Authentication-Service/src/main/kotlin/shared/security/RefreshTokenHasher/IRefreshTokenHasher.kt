package com.collektar.shared.security.RefreshTokenHasher

interface IRefreshTokenHasher {
    fun hash(refreshToken: String): String
}