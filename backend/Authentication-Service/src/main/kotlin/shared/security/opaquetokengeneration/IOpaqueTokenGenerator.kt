package com.collektar.shared.security.opaquetokengeneration

import com.collektar.shared.security.JWTService.RefreshToken
import java.util.*

interface IOpaqueTokenGenerator {
    fun generate(userId: UUID): RefreshToken
}