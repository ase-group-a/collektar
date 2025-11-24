package com.collektar.integration.shared

class OauthTokenCache {
    @Volatile
    private var cached: CachedToken? = null

    data class CachedToken(val token: String, val expiresAtEpochMillis: Long)

    @Synchronized
    fun getIfValid(): String? {
        val c = cached ?: return null
        if (System.currentTimeMillis() < c.expiresAtEpochMillis) return c.token
        cached = null
        return null
    }

    @Synchronized
    fun put(token: String, expiresInSeconds: Long) {
        val expiry = System.currentTimeMillis() + (expiresInSeconds * 1000L) - 5_000L // 5s safety
        cached = CachedToken(token, expiry)
    }

    @Synchronized
    fun clear() {
        cached = null
    }
}
