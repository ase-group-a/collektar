package com.collektar.integration.shared

import java.util.concurrent.ConcurrentHashMap


class OauthTokenCache {
    
    private val cachedTokens = ConcurrentHashMap<String, CachedToken>()

    data class CachedToken(val token: String, val expiresAtEpochMillis: Long)

    @Synchronized
    fun getIfValid(tokenName: String): String? {
        val c = cachedTokens[tokenName] ?: return null
        if (System.currentTimeMillis() < c.expiresAtEpochMillis) return c.token
        cachedTokens.remove(tokenName)
        return null
    }

    @Synchronized
    fun put(tokenName: String, token: String, expiresInSeconds: Long) {
        val expiry = System.currentTimeMillis() + (expiresInSeconds * 1000L) - 5_000L // 5s safety
        cachedTokens[tokenName] = CachedToken(token, expiry)
    }

    @Synchronized
    fun clear() {
        cachedTokens.clear()
    }
    
    @Synchronized
    fun clearToken(tokenName: String) {
        cachedTokens.remove(tokenName)
    }
}
