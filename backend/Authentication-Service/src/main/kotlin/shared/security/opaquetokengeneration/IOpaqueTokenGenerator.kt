package com.collektar.shared.security.opaquetokengeneration

import com.collektar.shared.security.jwt.RefreshToken
import java.util.*

interface IOpaqueTokenGenerator {
    fun generate(userId: UUID): RefreshToken
    fun generateRaw(byteLength: Int = 32): RawPasswordResetToken
}