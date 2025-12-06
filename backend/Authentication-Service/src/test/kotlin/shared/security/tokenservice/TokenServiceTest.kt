package com.collektar.shared.security.tokenservice

import com.auth0.jwt.interfaces.Claim
import com.auth0.jwt.interfaces.DecodedJWT
import com.collektar.features.auth.repository.AuthModel
import com.collektar.features.auth.repository.IAuthRepository
import com.collektar.features.auth.repository.StoredRefreshToken
import com.collektar.shared.errors.AppError
import com.collektar.shared.security.jwt.AccessToken
import com.collektar.shared.security.jwt.IJWTService
import com.collektar.shared.security.jwt.RefreshToken
import com.collektar.shared.security.opaquetokengeneration.IOpaqueTokenGenerator
import com.collektar.shared.security.tokenhasher.IRefreshTokenHasher
import io.mockk.*
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.time.Instant
import java.util.*
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class TokenServiceTest {
    private lateinit var jwtService: IJWTService
    private lateinit var tokenHasher: IRefreshTokenHasher
    private lateinit var opaqueTokenGenerator: IOpaqueTokenGenerator
    private lateinit var repository: IAuthRepository
    private lateinit var tokenService: ITokenService

    @BeforeEach
    fun setup() {
        jwtService = mockk()
        tokenHasher = mockk()
        opaqueTokenGenerator = mockk()
        repository = mockk()
        tokenService = TokenService(jwtService, tokenHasher, opaqueTokenGenerator, repository)
    }

    @AfterEach
    fun tearDown() {
        clearAllMocks()
    }

    @Test
    fun shouldCreateTokens() = runTest {
        val userId = UUID.randomUUID()
        val email = "test@email.com"
        val now = Instant.now()

        val accessToken = AccessToken(
            token = "access_token",
            expiresAt = now.plusSeconds(3600),
            issuedAt = now,
            userId = userId,
            email = email
        )

        val refreshToken = RefreshToken(
            token = "refresh_token",
            expiresAt = now.plusSeconds(86400),
            issuedAt = now,
            userId = userId
        )

        val tokenHash = "token_hash"

        every { jwtService.generateAccessToken(userId, email) } returns accessToken
        every { opaqueTokenGenerator.generate(userId) } returns refreshToken
        every { tokenHasher.hash(refreshToken.token) } returns tokenHash
        coEvery {
            repository.saveRefreshToken(userId, tokenHash, refreshToken.expiresAt, refreshToken.issuedAt)
        } just runs

        val result = tokenService.generateTokens(userId, email)

        assertEquals(accessToken.token, result.accessToken)
        assertEquals(refreshToken.token, result.refreshToken)
        assertTrue(result.accessTokenExpiresIn in 3599000L..3600000L)
        assertTrue(result.refreshTokenExpiresIn in 86399000L..86400000L)

        verify(exactly = 1) { jwtService.generateAccessToken(userId, email) }
        verify(exactly = 1) { opaqueTokenGenerator.generate(userId) }
        verify(exactly = 1) { tokenHasher.hash(refreshToken.token) }
        coVerify(exactly = 1) {
            repository.saveRefreshToken(userId, tokenHash, refreshToken.expiresAt, refreshToken.issuedAt)
        }
    }

    @Test
    fun shouldGenerateNewAndRevokeOldTokens() = runTest {
        val userId = UUID.randomUUID()
        val email = "test@example.com"
        val oldToken = "old_refresh_token"
        val tokenHash = "hashed_old_token"
        val now = Instant.now()

        val storedToken = StoredRefreshToken(
            tokenHash = tokenHash,
            userId = userId,
            expiresAt = now.plusSeconds(86400),
            issuedAt = now.minusSeconds(3600)
        )

        val user = AuthModel(
            id = userId,
            username = "testuser",
            email = email,
            displayName = "Test User",
            passwordHash = "hash"
        )

        val newAccessToken = AccessToken(
            token = "new_jwt_token",
            expiresAt = now.plusSeconds(3600),
            issuedAt = now,
            userId = userId,
            email = email
        )

        val newRefreshToken = RefreshToken(
            token = "new_refresh_token",
            expiresAt = now.plusSeconds(86400),
            issuedAt = now,
            userId = userId
        )

        val newTokenHash = "hashed_new_token"

        every { tokenHasher.hash(oldToken) } returns tokenHash
        coEvery { repository.findRefreshToken(tokenHash) } returns storedToken
        coEvery { repository.updateLastUsed(tokenHash) } just runs
        coEvery { repository.revokeRefreshToken(tokenHash) } just runs
        coEvery { repository.findByUserId(userId) } returns user
        every { jwtService.generateAccessToken(userId, email) } returns newAccessToken
        every { opaqueTokenGenerator.generate(userId) } returns newRefreshToken
        every { tokenHasher.hash(newRefreshToken.token) } returns newTokenHash
        coEvery {
            repository.saveRefreshToken(userId, newTokenHash, newRefreshToken.expiresAt, newRefreshToken.issuedAt)
        } just runs

        val result = tokenService.validateAndRefresh(oldToken)

        assertEquals(newAccessToken.token, result.accessToken)
        assertEquals(newRefreshToken.token, result.refreshToken)
        assertTrue(result.accessTokenExpiresIn in 3599000L..3600000L)
        assertTrue(result.refreshTokenExpiresIn in 86399000L..86400000L)

        coVerify(exactly = 1) { repository.findRefreshToken(tokenHash) }
        coVerify(exactly = 1) { repository.revokeRefreshToken(tokenHash) }
        coVerify(exactly = 1) { repository.findByUserId(userId) }
    }

    @Test
    fun shouldThrowUnauthorizedInvalidTokenIfTokenNotFound() = runTest {
        val token = "invalid_token"
        val tokenHash = "hashed_invalid_token"

        every { tokenHasher.hash(token) } returns tokenHash
        coEvery { repository.findRefreshToken(tokenHash) } returns null

        assertThrows<AppError.Unauthorized.InvalidToken> {
            tokenService.validateAndRefresh(token)
        }

        coVerify(exactly = 1) { repository.findRefreshToken(tokenHash) }
        coVerify(exactly = 0) { repository.revokeRefreshToken(any()) }
    }

    @Test
    fun shouldThrowUnauthorizedInvalidToknIfTokenIsExpired() = runTest {
        val token = "expired_token"
        val tokenHash = "hashed_expired_token"
        val userId = UUID.randomUUID()
        val now = Instant.now()

        val storedToken = StoredRefreshToken(
            tokenHash = tokenHash,
            userId = userId,
            expiresAt = now.minusSeconds(3600),
            issuedAt = now.minusSeconds(86400)
        )

        every { tokenHasher.hash(token) } returns tokenHash
        coEvery { repository.findRefreshToken(tokenHash) } returns storedToken
        coEvery { repository.revokeRefreshToken(tokenHash) } just runs

        assertThrows<AppError.Unauthorized.InvalidToken> {
            tokenService.validateAndRefresh(token)
        }

        coVerify(exactly = 1) { repository.findRefreshToken(tokenHash) }
        coVerify(exactly = 1) { repository.revokeRefreshToken(tokenHash) }
    }

    @Test
    fun shouldThrowUnauthorizedInvalidTokenIfUserNotFound() = runTest {
        val token = "valid_token"
        val tokenHash = "hashed_token"
        val userId = UUID.randomUUID()
        val now = Instant.now()

        val storedToken = StoredRefreshToken(
            tokenHash = tokenHash,
            userId = userId,
            expiresAt = now.plusSeconds(86400),
            issuedAt = now.minusSeconds(3600)
        )

        every { tokenHasher.hash(token) } returns tokenHash
        coEvery { repository.findRefreshToken(tokenHash) } returns storedToken
        coEvery { repository.updateLastUsed(tokenHash) } just runs
        coEvery { repository.revokeRefreshToken(tokenHash) } just runs
        coEvery { repository.findByUserId(userId) } returns null

        assertThrows<AppError.Unauthorized.InvalidToken> {
            tokenService.validateAndRefresh(token)
        }

        coVerify(exactly = 1) { repository.revokeRefreshToken(tokenHash) }
        coVerify(exactly = 1) { repository.findByUserId(userId) }
    }

    @Test
    fun shouldReturnTokenClaims() = runTest {
        val token = "valid_jwt_token"
        val userId = UUID.randomUUID()
        val email = "test@example.com"

        val decodedJWT = mockk<DecodedJWT>()
        val typeClaim = mockk<Claim>()
        val userIdClaim = mockk<Claim>()
        val emailClaim = mockk<Claim>()

        every { jwtService.verify(token) } returns decodedJWT
        every { decodedJWT.getClaim("type") } returns typeClaim
        every { typeClaim.asString() } returns "access"
        every { decodedJWT.getClaim("userId") } returns userIdClaim
        every { userIdClaim.asString() } returns userId.toString()
        every { decodedJWT.getClaim("email") } returns emailClaim
        every { emailClaim.asString() } returns email

        val result = tokenService.validateAccessToken(token)

        assertEquals(userId, result.userId)
        assertEquals(email, result.email)

        verify(exactly = 1) { jwtService.verify(token) }
    }

    @Test
    fun shouldThrowUnauthorizedInvalidTokenIfJWTVerifyFails() = runTest {
        val token = "invalid_jwt"

        every { jwtService.verify(token) } returns null

        assertThrows<AppError.Unauthorized.InvalidToken> {
            tokenService.validateAccessToken(token)
        }

        verify(exactly = 1) { jwtService.verify(token) }
    }

    @Test
    fun shouldThrowUnauthorizedInvalidTokenIfTokenTypeIsNotAccess() = runTest {
        val token = "refresh_jwt_token"

        val decodedJWT = mockk<DecodedJWT>()
        val typeClaim = mockk<Claim>()

        every { jwtService.verify(token) } returns decodedJWT
        every { decodedJWT.getClaim("type") } returns typeClaim
        every { typeClaim.asString() } returns "refresh"

        assertThrows<AppError.Unauthorized.InvalidToken> {
            tokenService.validateAccessToken(token)
        }
    }

    @Test
    fun shouldThrowUnauthorizedInvalidTokenIfUSerIdClaimIsMissing() = runTest {
        val token = "malformed_jwt"

        val decodedJWT = mockk<DecodedJWT>()
        val typeClaim = mockk<Claim>()
        val userIdClaim = mockk<Claim>()

        every { jwtService.verify(token) } returns decodedJWT
        every { decodedJWT.getClaim("type") } returns typeClaim
        every { typeClaim.asString() } returns "access"
        every { decodedJWT.getClaim("userId") } returns userIdClaim
        every { userIdClaim.asString() } returns null

        assertThrows<AppError.Unauthorized.InvalidToken> {
            tokenService.validateAccessToken(token)
        }
    }

    @Test
    fun shouldThrowUnauthorizedInvalidTokenIfUSerIdIsNotUUID() = runTest {
        val token = "invalid_uuid_jwt"

        val decodedJWT = mockk<DecodedJWT>()
        val typeClaim = mockk<Claim>()
        val userIdClaim = mockk<Claim>()
        val emailClaim = mockk<Claim>()

        every { jwtService.verify(token) } returns decodedJWT
        every { decodedJWT.getClaim("type") } returns typeClaim
        every { typeClaim.asString() } returns "access"
        every { decodedJWT.getClaim("userId") } returns userIdClaim
        every { userIdClaim.asString() } returns "not-a-uuid"
        every { decodedJWT.getClaim("email") } returns emailClaim
        every { emailClaim.asString() } returns "test@example.com"

        assertThrows<AppError.Unauthorized.InvalidToken> {
            tokenService.validateAccessToken(token)
        }
    }

    @Test
    fun shouldThrowUnauthorizedInvalidTokenIfEmailClaimIsMissing() = runTest {
        val token = "no_email_jwt"
        val userId = UUID.randomUUID()

        val decodedJWT = mockk<DecodedJWT>()
        val typeClaim = mockk<Claim>()
        val userIdClaim = mockk<Claim>()
        val emailClaim = mockk<Claim>()

        every { jwtService.verify(token) } returns decodedJWT
        every { decodedJWT.getClaim("type") } returns typeClaim
        every { typeClaim.asString() } returns "access"
        every { decodedJWT.getClaim("userId") } returns userIdClaim
        every { userIdClaim.asString() } returns userId.toString()
        every { decodedJWT.getClaim("email") } returns emailClaim
        every { emailClaim.asString() } returns null

        assertThrows<AppError.Unauthorized.InvalidToken> {
            tokenService.validateAccessToken(token)
        }
    }

    @Test
    fun shouldHashTokenAndRevokeIt() = runTest {
        val token = "refresh_token"
        val tokenHash = "hashed_token"

        every { tokenHasher.hash(token) } returns tokenHash
        coEvery { repository.revokeRefreshToken(tokenHash) } just runs

        tokenService.revokeRefreshToken(token)

        verify(exactly = 1) { tokenHasher.hash(token) }
        coVerify(exactly = 1) { repository.revokeRefreshToken(tokenHash) }
    }
}