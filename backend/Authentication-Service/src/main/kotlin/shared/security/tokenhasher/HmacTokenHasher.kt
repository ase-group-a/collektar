package com.collektar.shared.security.tokenhasher

import com.collektar.config.TokenHasherConfig
import java.util.*
import javax.crypto.Mac

class HmacTokenHasher(private val config: TokenHasherConfig) : IRefreshTokenHasher {
    override fun hash(refreshToken: String): String {
        val mac = Mac.getInstance(config.algorithm).apply { init(config.secret) }
        val hashBytes = mac.doFinal(refreshToken.toByteArray(Charsets.UTF_8))
        return Base64.getEncoder().encodeToString(hashBytes)
    }
}