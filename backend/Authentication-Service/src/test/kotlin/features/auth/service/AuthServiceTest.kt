package com.collektar.features.auth.service

import com.collektar.dto.LoginRequest
import com.collektar.dto.RefreshTokenRequest
import com.collektar.dto.RegisterRequest
import com.collektar.features.auth.repository.AuthModel
import com.collektar.features.auth.repository.IAuthRepository
import com.collektar.shared.errors.AppError
import com.collektar.shared.security.passwordhasher.IPasswordHasher
import com.collektar.shared.security.tokenservice.ITokenService
import com.collektar.shared.security.tokenservice.TokenClaims
import com.collektar.shared.security.tokenservice.TokenPair
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.mockk.*
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.util.*
import kotlin.test.assertEquals

class AuthServiceTest {
    private lateinit var repository: IAuthRepository
    private lateinit var tokenService: ITokenService
    private lateinit var passwordHasher: IPasswordHasher
    private lateinit var authService: AuthService

    @BeforeEach
    fun setup() {
        repository = mockk()
        tokenService = mockk()
        passwordHasher = mockk()
        authService = AuthService(repository, tokenService, passwordHasher)
    }

    @AfterEach
    fun tearDown() {
        clearAllMocks()
    }

    @Test
    fun shouldCreateUserForValidRequest() = runTest {
        val request = RegisterRequest(
            email = "email@example.com",
            username = "newuser",
            displayName = "displayname",
            password = "password"
        )
        val passwordHash = "passwordHash"
        val tokenPair = TokenPair(
            accessToken = "access_token",
            accessTokenExpiresIn = 3600,
            refreshToken = "refresh_token",
            refreshTokenExpiresIn = 86400,
        )

        coEvery { repository.usernameExists(request.username) } returns false
        coEvery { repository.emailExists(request.email) } returns false
        every { passwordHasher.hash(request.password) } returns passwordHash
        coEvery {
            repository.createUser(any(), any(), any(), any(), any())
        } returns AuthModel(
            id = UUID.randomUUID(),
            username = request.username,
            email = request.email,
            displayName = request.displayName,
            passwordHash = passwordHash,
        )
        coEvery { tokenService.generateTokens(any(), any()) } returns tokenPair

        val result = authService.register(request)

        assertEquals(tokenPair.accessToken, result.accessTokenResponse.accessToken)
        assertEquals(tokenPair.accessTokenExpiresIn, result.accessTokenResponse.expiresIn)
        assertEquals(tokenPair.refreshToken, result.refreshToken)
        assertEquals(tokenPair.refreshTokenExpiresIn, result.refreshTokenExpiresIn)
        assertEquals(request.username, result.accessTokenResponse.user.username)
        assertEquals(request.email, result.accessTokenResponse.user.email)
        assertEquals(request.displayName, result.accessTokenResponse.user.displayName)

        coVerify(exactly = 1) { repository.usernameExists(request.username) }
        coVerify(exactly = 1) { repository.emailExists(request.email) }
        verify(exactly = 1) { passwordHasher.hash(request.password) }
        coVerify(exactly = 1) {
            repository.createUser(
                userId = any(),
                username = request.username,
                email = request.email,
                displayName = request.displayName,
                passwordHash = passwordHash
            )
        }
        coVerify(exactly = 1) { tokenService.generateTokens(any(), any()) }
    }

    @Test
    fun shouldThrowConflictUserNameTaken() = runTest {
        val request = RegisterRequest(
            email = "email@example.com",
            username = "newuser",
            displayName = "displayname",
            password = "password"
        )
        coEvery { repository.usernameExists(request.username) } returns true

        assertThrows<AppError.Conflict.UsernameTaken> {
            authService.register(request)
        }

        coVerify(exactly = 1) { repository.usernameExists(request.username) }
        coVerify(exactly = 0) { repository.emailExists(any()) }
        verify(exactly = 0) { passwordHasher.hash(any()) }
        coVerify(exactly = 0) { tokenService.generateTokens(any(), any()) }
    }

    @Test
    fun shouldThrowConflictEmailAlreadyInUse() = runTest {
        val request = RegisterRequest(
            email = "email@example.com",
            username = "newuser",
            displayName = "displayname",
            password = "password"
        )
        coEvery { repository.usernameExists(request.username) } returns false
        coEvery { repository.emailExists(request.email) } returns true

        assertThrows<AppError.Conflict.EmailAlreadyInUse> {
            authService.register(request)
        }

        coVerify(exactly = 1) { repository.usernameExists(request.username) }
        coVerify(exactly = 1) { repository.emailExists(any()) }
        verify(exactly = 0) { passwordHasher.hash(any()) }
        coVerify(exactly = 0) { tokenService.generateTokens(any(), any()) }
    }

    @Test
    fun shouldReturnTokensIfCredentialsAreValid() = runTest {
        val request = LoginRequest("username", "password")
        val userId = UUID.randomUUID()
        val user = AuthModel(
            id = userId,
            username = "username",
            email = "test@example.com",
            displayName = "test user",
            passwordHash = "passwordHash"
        )
        val tokenPair = TokenPair(
            accessToken = "access_token",
            accessTokenExpiresIn = 3600,
            refreshToken = "refresh_token",
            refreshTokenExpiresIn = 86400,
        )

        coEvery { repository.findByUsername(request.username) } returns user
        every { passwordHasher.verify(request.password, user.passwordHash) } returns true
        coEvery { repository.revokeAllUserTokens(userId) } just runs
        coEvery { tokenService.generateTokens(userId, user.email) } returns tokenPair

        val result = authService.login(request)

        assertEquals(tokenPair.accessToken, result.accessTokenResponse.accessToken)
        assertEquals(tokenPair.accessTokenExpiresIn, result.accessTokenResponse.expiresIn)
        assertEquals(tokenPair.refreshToken, result.refreshToken)
        assertEquals(tokenPair.refreshTokenExpiresIn, result.refreshTokenExpiresIn)

        coVerify(exactly = 1) { repository.findByUsername(request.username) }
        verify(exactly = 1) { passwordHasher.verify(request.password, user.passwordHash) }
        coVerify(exactly = 1) { repository.revokeAllUserTokens(userId) }
        coVerify(exactly = 1) { tokenService.generateTokens(userId, user.email) }
    }

    @Test
    fun shouldThrowUnauthorizedInvalidCredentialsIfUsernameDoesNotExist() = runTest {
        val request = LoginRequest("username", "password")

        coEvery { repository.findByUsername(request.username) } returns null

        assertThrows<AppError.Unauthorized.InvalidCredentials> {
            authService.login(request)
        }

        coVerify(exactly = 1) { repository.findByUsername(request.username) }
        verify(exactly = 0) { passwordHasher.verify(request.password, any()) }
        coVerify(exactly = 0) { repository.revokeAllUserTokens(any()) }
        coVerify(exactly = 0) { tokenService.generateTokens(any(), any()) }
    }

    @Test
    fun shouldThrowUnauthorizedInvalidCredentialsIfPasswordIsWrong() = runTest {
        val request = LoginRequest("username", "password")
        val userId = UUID.randomUUID()
        val user = AuthModel(
            id = userId,
            username = "username",
            email = "test@example.com",
            displayName = "test user",
            passwordHash = "passwordHash"
        )

        coEvery { repository.findByUsername(request.username) } returns user
        coEvery { passwordHasher.verify(request.password, user.passwordHash) } returns false

        assertThrows<AppError.Unauthorized.InvalidCredentials> {
            authService.login(request)
        }

        coVerify(exactly = 1) { repository.findByUsername(request.username) }
        verify(exactly = 1) { passwordHasher.verify(request.password, user.passwordHash) }
        coVerify(exactly = 0) { repository.revokeAllUserTokens(userId) }
        coVerify(exactly = 0) { tokenService.generateTokens(userId, user.email) }
    }

    @Test
    fun shouldReturnTokensIfRefreshRequestIsValid() = runTest {
        val request = RefreshTokenRequest(refreshToken = "current_refresh_token")
        val userId = UUID.randomUUID()

        val newTokenPair = TokenPair(
            accessToken = "new_access_token",
            accessTokenExpiresIn = 3600,
            refreshToken = "refresh_token",
            refreshTokenExpiresIn = 86400,
        )

        val claims = TokenClaims(
            userId = userId,
            email = "user@mail.com"
        )

        val authModel = AuthModel(
            id = userId,
            username = "testuser",
            email = "user@mail.com",
            displayName = "Test User",
            passwordHash = "irrelevant"
        )

        coEvery { tokenService.validateAndRefresh(request.refreshToken) } returns newTokenPair
        coEvery { tokenService.validateAccessToken(newTokenPair.accessToken) } returns claims
        coEvery { repository.findByUserId(claims.userId) } returns authModel

        val result = authService.refresh(request)

        assertEquals(newTokenPair.accessToken, result.accessTokenResponse.accessToken)
        assertEquals(newTokenPair.accessTokenExpiresIn, result.accessTokenResponse.expiresIn)
        assertEquals(newTokenPair.refreshToken, result.refreshToken)
        assertEquals(newTokenPair.refreshTokenExpiresIn, result.refreshTokenExpiresIn)

        coVerify(exactly = 1) { tokenService.validateAndRefresh(request.refreshToken) }
    }

    @Test
    fun shouldSetUserHeadersWhenAccessTokenIsValid() = runTest {
        val token = "valid_access_token"
        val userId = UUID.randomUUID()
        val email = "test@example.com"

        val routingCall = mockk<RoutingCall>(relaxed = true)
        val headers = mockk<ResponseHeaders>(relaxed = true)

        coEvery { tokenService.validateAccessToken(token) } returns TokenClaims(userId, email)
        every { routingCall.response.headers } returns headers

        authService.verify(token, routingCall)

        coVerify(exactly = 1) { tokenService.validateAccessToken(token) }
        coVerify(exactly = 1) { headers.append("X-User-Id", userId.toString()) }
        coVerify(exactly = 1) { headers.append("X-User-Email", email) }
    }

    @Test
    fun shouldCallTokenServiceRevokeRefreshTokenOnLogout() = runTest {
        val refreshToken = "refresh_token"
        coEvery { tokenService.revokeRefreshToken(refreshToken) } just runs
        authService.logout(refreshToken)

        coVerify(exactly = 1) { tokenService.revokeRefreshToken(refreshToken) }
    }

}