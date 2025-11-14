package com.collektar.shared.security.RefreshTokenHasher

import com.collektar.config.TokenHasherConfig
import java.util.*
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec

class HmacTokenHasher(private val config: TokenHasherConfig) : IRefreshTokenHasher {
    override fun hash(refreshToken: String): String {
        val mac = Mac.getInstance(config.algorithm)
        val keySpec = SecretKeySpec(config.secret.toByteArray(Charsets.UTF_8), config.algorithm)
        mac.init(keySpec)
        val hashBytes = mac.doFinal(refreshToken.toByteArray(Charsets.UTF_8))
        return Base64.getEncoder().encodeToString(hashBytes)
    }
}