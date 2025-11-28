package com.collektar.integration.shared

class OauthTokenCache {
    @Volatile
    private var cachedToken: CachedToken? = null

    data class CachedToken(val token: String, val expiresAtEpochMillis: Long)

    @Synchronized
    fun getIfValid(): String? {
        val c = cachedToken ?: return null
        if (System.currentTimeMillis() < c.expiresAtEpochMillis) return c.token
        cachedToken = null
        return null
    }

    @Synchronized
    fun put(token: String, expiresInSeconds: Long) {
        val expiry = System.currentTimeMillis() + (expiresInSeconds * 1000L) - 5_000L // 5s safety
        cachedToken = CachedToken(token, expiry)
    }

    @Synchronized
    fun clear() {
        cachedToken = null
    }
}
