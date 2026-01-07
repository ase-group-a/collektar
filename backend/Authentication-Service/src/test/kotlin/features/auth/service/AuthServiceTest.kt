package com.collektar.features.auth.service

import com.collektar.dto.*
import com.collektar.features.auth.repository.AuthModel
import com.collektar.features.auth.repository.IAuthRepository
import com.collektar.shared.email.EmailService
import com.collektar.shared.errors.AppError
import com.collektar.shared.security.passwordhasher.IPasswordHasher
import com.collektar.shared.security.tokenservice.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.mockk.*
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.time.Instant
import java.util.*
import kotlin.test.assertEquals

class AuthServiceTest {
    private lateinit var repository: IAuthRepository
    private lateinit var tokenService: ITokenService
    private lateinit var passwordHasher: IPasswordHasher
    private lateinit var authService: AuthService
    private lateinit var emailService: EmailService

    @BeforeEach
    fun setup() {
        repository = mockk()
        tokenService = mockk()
        passwordHasher = mockk()
        emailService = mockk()
        authService = AuthService(repository, tokenService, passwordHasher, emailService)
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
        coEvery { repository.createUser(any(), any(), any(), any(), any()) } returns AuthModel(
            id = UUID.randomUUID(),
            username = request.username,
            email = request.email,
            displayName = request.displayName,
            passwordHash = passwordHash,
        )
        coEvery { tokenService.generateTokens(any(), any()) } returns tokenPair
        every { emailService.sendWelcomeEmail(any(), any()) } just runs

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
        verify(exactly = 1) { emailService.sendWelcomeEmail(request.email, request.displayName) }
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
        verify(exactly = 0) { emailService.sendWelcomeEmail(any(), any()) }
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
        verify(exactly = 0) { emailService.sendWelcomeEmail(any(), any()) }
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

    @Test
    fun shouldSendPasswordResetEmailWhenUserExists() = runTest {
        val request = ForgotPasswordRequest(email = "user@example.com")
        val userId = UUID.randomUUID()
        val user = AuthModel(
            id = userId,
            username = "testuser",
            email = "user@example.com",
            displayName = "Test User",
            passwordHash = "hash"
        )
        val resetToken = PasswordResetToken(
            token = "reset_token_123",
            expiresAt = Instant.now().plusMillis(30 * 60L),
            validityMinutes = 30
        )

        coEvery { repository.findByEmail(request.email) } returns user
        coEvery { tokenService.generatePasswordResetToken(userId) } returns resetToken
        every { emailService.sendPasswordResetEmail(any(), any(), any(), any()) } returns Result.success(Unit)

        authService.forgotPassword(request)

        coVerify(exactly = 1) { repository.findByEmail(request.email) }
        coVerify(exactly = 1) { tokenService.generatePasswordResetToken(userId) }
        verify(exactly = 1) {
            emailService.sendPasswordResetEmail(
                to = user.email,
                displayName = user.displayName,
                resetToken = resetToken.token,
                expiryMinutes = resetToken.validityMinutes
            )
        }
    }

    @Test
    fun shouldNotSendEmailWhenUserDoesNotExistInForgotPassword() = runTest {
        val request = ForgotPasswordRequest(email = "nonexistent@example.com")

        coEvery { repository.findByEmail(request.email) } returns null

        authService.forgotPassword(request)

        coVerify(exactly = 1) { repository.findByEmail(request.email) }
        coVerify(exactly = 0) { tokenService.generatePasswordResetToken(any()) }
        verify(exactly = 0) { emailService.sendPasswordResetEmail(any(), any(), any(), any()) }
    }

    @Test
    fun shouldResetPasswordAndRevokeTokensWhenValidToken() = runTest {
        val userId = UUID.randomUUID()
        val request = ResetPasswordRequest(
            token = "valid_reset_token",
            newPassword = "newPassword123"
        )
        val claims = PasswordResetTokenClaims(userId = userId, tokenId = UUID.randomUUID())
        val user = AuthModel(
            id = userId,
            username = "testuser",
            email = "user@example.com",
            displayName = "Test User",
            passwordHash = "oldHash"
        )
        val newPasswordHash = "newPasswordHash"

        coEvery { tokenService.consumePasswordResetToken(request.token) } returns claims
        coEvery { repository.findByUserId(userId) } returns user
        every { passwordHasher.hash(request.newPassword) } returns newPasswordHash
        coEvery { repository.updatePassword(userId, newPasswordHash) } just runs
        coEvery { repository.revokeAllUserTokens(userId) } just runs
        every { emailService.sendPasswordChangedEmail(any(), any()) } returns Result.success(Unit)

        authService.resetPassword(request)

        coVerify(exactly = 1) { tokenService.consumePasswordResetToken(request.token) }
        coVerify(exactly = 1) { repository.findByUserId(userId) }
        verify(exactly = 1) { passwordHasher.hash(request.newPassword) }
        coVerify(exactly = 1) { repository.updatePassword(userId, newPasswordHash) }
        coVerify(exactly = 1) { repository.revokeAllUserTokens(userId) }
        verify(exactly = 1) {
            emailService.sendPasswordChangedEmail(
                to = user.email,
                displayName = user.displayName
            )
        }
    }

    @Test
    fun shouldThrowUserNotFoundWhenResettingPasswordForNonexistentUser() = runTest {
        val userId = UUID.randomUUID()
        val request = ResetPasswordRequest(
            token = "valid_reset_token",
            newPassword = "newPassword123"
        )
        val claims = PasswordResetTokenClaims(userId = userId, tokenId = UUID.randomUUID())

        coEvery { tokenService.consumePasswordResetToken(request.token) } returns claims
        coEvery { repository.findByUserId(userId) } returns null

        assertThrows<AppError.NotFound.UserNotFound> {
            authService.resetPassword(request)
        }

        coVerify(exactly = 1) { tokenService.consumePasswordResetToken(request.token) }
        coVerify(exactly = 1) { repository.findByUserId(userId) }
        verify(exactly = 0) { passwordHasher.hash(any()) }
        coVerify(exactly = 0) { repository.updatePassword(any(), any()) }
    }

    @Test
    fun shouldChangePasswordWhenCurrentPasswordIsCorrect() = runTest {
        val userId = UUID.randomUUID()
        val request = ChangePasswordRequest(
            currentPassword = "oldPassword",
            newPassword = "newPassword123"
        )
        val user = AuthModel(
            id = userId,
            username = "testuser",
            email = "user@example.com",
            displayName = "Test User",
            passwordHash = "oldPasswordHash"
        )
        val newPasswordHash = "newPasswordHash"

        coEvery { repository.findByUserId(userId) } returns user
        every { passwordHasher.verify(request.currentPassword, user.passwordHash) } returns true
        every { passwordHasher.hash(request.newPassword) } returns newPasswordHash
        coEvery { repository.updatePassword(userId, newPasswordHash) } just runs
        coEvery { repository.revokeAllUserTokens(userId) } just runs
        coEvery { repository.deletePasswordResetTokensOfUser(userId) } just runs
        every { emailService.sendPasswordChangedEmail(any(), any()) } returns Result.success(Unit)

        authService.changePassword(userId, request)

        coVerify(exactly = 1) { repository.findByUserId(userId) }
        verify(exactly = 1) { passwordHasher.verify(request.currentPassword, user.passwordHash) }
        verify(exactly = 1) { passwordHasher.hash(request.newPassword) }
        coVerify(exactly = 1) { repository.updatePassword(userId, newPasswordHash) }
        coVerify(exactly = 1) { repository.revokeAllUserTokens(userId) }
        coVerify(exactly = 1) { repository.deletePasswordResetTokensOfUser(userId) }
        verify(exactly = 1) {
            emailService.sendPasswordChangedEmail(
                to = user.email,
                displayName = user.displayName
            )
        }
    }

    @Test
    fun shouldThrowUserNotFoundWhenChangingPasswordForNonexistentUser() = runTest {
        val userId = UUID.randomUUID()
        val request = ChangePasswordRequest(
            currentPassword = "oldPassword",
            newPassword = "newPassword123"
        )

        coEvery { repository.findByUserId(userId) } returns null

        assertThrows<AppError.NotFound.UserNotFound> {
            authService.changePassword(userId, request)
        }

        coVerify(exactly = 1) { repository.findByUserId(userId) }
        verify(exactly = 0) { passwordHasher.verify(any(), any()) }
        verify(exactly = 0) { passwordHasher.hash(any()) }
    }

    @Test
    fun shouldThrowInvalidCredentialsWhenCurrentPasswordIsWrong() = runTest {
        val userId = UUID.randomUUID()
        val request = ChangePasswordRequest(
            currentPassword = "wrongPassword",
            newPassword = "newPassword123"
        )
        val user = AuthModel(
            id = userId,
            username = "testuser",
            email = "user@example.com",
            displayName = "Test User",
            passwordHash = "oldPasswordHash"
        )

        coEvery { repository.findByUserId(userId) } returns user
        every { passwordHasher.verify(request.currentPassword, user.passwordHash) } returns false

        assertThrows<AppError.Unauthorized.InvalidCredentials> {
            authService.changePassword(userId, request)
        }

        coVerify(exactly = 1) { repository.findByUserId(userId) }
        verify(exactly = 1) { passwordHasher.verify(request.currentPassword, user.passwordHash) }
        verify(exactly = 0) { passwordHasher.hash(any()) }
        coVerify(exactly = 0) { repository.updatePassword(any(), any()) }
    }

    @Test
    fun shouldDeleteAccountWhenPasswordIsCorrect() = runTest {
        val userId = UUID.randomUUID()
        val request = DeleteAccountRequest(password = "correctPassword")
        val user = AuthModel(
            id = userId,
            username = "testuser",
            email = "user@example.com",
            displayName = "Test User",
            passwordHash = "passwordHash"
        )

        coEvery { repository.findByUserId(userId) } returns user
        every { passwordHasher.verify(request.password, user.passwordHash) } returns true
        coEvery { repository.deleteUser(userId) } just runs
        every { emailService.sendAccountDeletedEmail(any(), any()) } returns Result.success(Unit)

        authService.deleteAccount(userId, request)

        coVerify(exactly = 1) { repository.findByUserId(userId) }
        verify(exactly = 1) { passwordHasher.verify(request.password, user.passwordHash) }
        coVerify(exactly = 1) { repository.deleteUser(userId) }
        verify(exactly = 1) {
            emailService.sendAccountDeletedEmail(
                to = user.email,
                displayName = user.displayName
            )
        }
    }

    @Test
    fun shouldThrowUserNotFoundWhenDeletingNonexistentAccount() = runTest {
        val userId = UUID.randomUUID()
        val request = DeleteAccountRequest(password = "password")

        coEvery { repository.findByUserId(userId) } returns null

        assertThrows<AppError.NotFound.UserNotFound> {
            authService.deleteAccount(userId, request)
        }

        coVerify(exactly = 1) { repository.findByUserId(userId) }
        verify(exactly = 0) { passwordHasher.verify(any(), any()) }
        coVerify(exactly = 0) { repository.deleteUser(any()) }
    }

    @Test
    fun shouldThrowInvalidCredentialsWhenDeletingAccountWithWrongPassword() = runTest {
        val userId = UUID.randomUUID()
        val request = DeleteAccountRequest(password = "wrongPassword")
        val user = AuthModel(
            id = userId,
            username = "testuser",
            email = "user@example.com",
            displayName = "Test User",
            passwordHash = "passwordHash"
        )

        coEvery { repository.findByUserId(userId) } returns user
        every { passwordHasher.verify(request.password, user.passwordHash) } returns false

        assertThrows<AppError.Unauthorized.InvalidCredentials> {
            authService.deleteAccount(userId, request)
        }

        coVerify(exactly = 1) { repository.findByUserId(userId) }
        verify(exactly = 1) { passwordHasher.verify(request.password, user.passwordHash) }
        coVerify(exactly = 0) { repository.deleteUser(any()) }
        verify(exactly = 0) { emailService.sendAccountDeletedEmail(any(), any()) }
    }

    @Test
    fun shouldThrowInvalidCredentialsWhenUserNotFoundDuringRefresh() = runTest {
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

        coEvery { tokenService.validateAndRefresh(request.refreshToken) } returns newTokenPair
        coEvery { tokenService.validateAccessToken(newTokenPair.accessToken) } returns claims
        coEvery { repository.findByUserId(claims.userId) } returns null

        assertThrows<AppError.Unauthorized.InvalidCredentials> {
            authService.refresh(request)
        }

        coVerify(exactly = 1) { tokenService.validateAndRefresh(request.refreshToken) }
        coVerify(exactly = 1) { tokenService.validateAccessToken(newTokenPair.accessToken) }
        coVerify(exactly = 1) { repository.findByUserId(claims.userId) }
    }

    @Test
    fun shouldPropagateExceptionWhenValidateAndRefreshFails() = runTest {
        val request = RefreshTokenRequest(refreshToken = "invalid_refresh_token")

        coEvery { tokenService.validateAndRefresh(request.refreshToken) } throws AppError.Unauthorized.InvalidToken()

        assertThrows<AppError.Unauthorized.InvalidToken> {
            authService.refresh(request)
        }

        coVerify(exactly = 1) { tokenService.validateAndRefresh(request.refreshToken) }
        coVerify(exactly = 0) { tokenService.validateAccessToken(any()) }
        coVerify(exactly = 0) { repository.findByUserId(any()) }
    }

    @Test
    fun shouldPropagateExceptionWhenValidateAccessTokenFailsInVerify() = runTest {
        val token = "invalid_access_token"
        val routingCall = mockk<RoutingCall>(relaxed = true)

        coEvery { tokenService.validateAccessToken(token) } throws AppError.Unauthorized.InvalidToken()

        assertThrows<AppError.Unauthorized.InvalidToken> {
            authService.verify(token, routingCall)
        }

        coVerify(exactly = 1) { tokenService.validateAccessToken(token) }
    }

    @Test
    fun shouldPropagateExceptionWhenGeneratePasswordResetTokenFails() = runTest {
        val request = ForgotPasswordRequest(email = "user@example.com")
        val userId = UUID.randomUUID()
        val user = AuthModel(
            id = userId,
            username = "testuser",
            email = "user@example.com",
            displayName = "Test User",
            passwordHash = "hash"
        )

        coEvery { repository.findByEmail(request.email) } returns user
        coEvery { tokenService.generatePasswordResetToken(userId) } throws Exception("Token generation failed")

        assertThrows<Exception> {
            authService.forgotPassword(request)
        }

        coVerify(exactly = 1) { repository.findByEmail(request.email) }
        coVerify(exactly = 1) { tokenService.generatePasswordResetToken(userId) }
        verify(exactly = 0) { emailService.sendPasswordResetEmail(any(), any(), any(), any()) }
    }

    @Test
    fun shouldPropagateExceptionWhenConsumePasswordResetTokenFails() = runTest {
        val request = ResetPasswordRequest(
            token = "invalid_reset_token",
            newPassword = "newPassword123"
        )

        coEvery { tokenService.consumePasswordResetToken(request.token) } throws AppError.Unauthorized.InvalidToken()

        assertThrows<AppError.Unauthorized.InvalidToken> {
            authService.resetPassword(request)
        }

        coVerify(exactly = 1) { tokenService.consumePasswordResetToken(request.token) }
        coVerify(exactly = 0) { repository.findByUserId(any()) }
        verify(exactly = 0) { passwordHasher.hash(any()) }
    }

    @Test
    fun shouldPropagateExceptionWhenRevokeRefreshTokenFails() = runTest {
        val refreshToken = "refresh_token"

        coEvery { tokenService.revokeRefreshToken(refreshToken) } throws Exception("Revocation failed")

        assertThrows<Exception> {
            authService.logout(refreshToken)
        }

        coVerify(exactly = 1) { tokenService.revokeRefreshToken(refreshToken) }
    }

    @Test
    fun shouldPropagateExceptionWhenCreateUserFails() = runTest {
        val request = RegisterRequest(
            email = "email@example.com",
            username = "newuser",
            displayName = "displayname",
            password = "password"
        )
        val passwordHash = "passwordHash"

        coEvery { repository.usernameExists(request.username) } returns false
        coEvery { repository.emailExists(request.email) } returns false
        every { passwordHasher.hash(request.password) } returns passwordHash
        coEvery { repository.createUser(any(), any(), any(), any(), any()) } throws Exception("Database error")

        assertThrows<Exception> {
            authService.register(request)
        }

        coVerify(exactly = 1) { repository.usernameExists(request.username) }
        coVerify(exactly = 1) { repository.emailExists(request.email) }
        verify(exactly = 1) { passwordHasher.hash(request.password) }
        coVerify(exactly = 1) { repository.createUser(any(), any(), any(), any(), any()) }
        coVerify(exactly = 0) { tokenService.generateTokens(any(), any()) }
        verify(exactly = 0) { emailService.sendWelcomeEmail(any(), any()) }
    }

    @Test
    fun shouldPropagateExceptionWhenGenerateTokensFailsInRegister() = runTest {
        val request = RegisterRequest(
            email = "email@example.com",
            username = "newuser",
            displayName = "displayname",
            password = "password"
        )
        val passwordHash = "passwordHash"

        coEvery { repository.usernameExists(request.username) } returns false
        coEvery { repository.emailExists(request.email) } returns false
        every { passwordHasher.hash(request.password) } returns passwordHash
        coEvery { repository.createUser(any(), any(), any(), any(), any()) } returns AuthModel(
            id = UUID.randomUUID(),
            username = request.username,
            email = request.email,
            displayName = request.displayName,
            passwordHash = passwordHash,
        )
        coEvery { tokenService.generateTokens(any(), any()) } throws Exception("Token generation failed")

        assertThrows<Exception> {
            authService.register(request)
        }

        coVerify(exactly = 1) { tokenService.generateTokens(any(), any()) }
        verify(exactly = 0) { emailService.sendWelcomeEmail(any(), any()) }
    }

    @Test
    fun shouldPropagateExceptionWhenRevokeAllUserTokensFailsInLogin() = runTest {
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
        every { passwordHasher.verify(request.password, user.passwordHash) } returns true
        coEvery { repository.revokeAllUserTokens(userId) } throws Exception("Revocation failed")

        assertThrows<Exception> {
            authService.login(request)
        }

        coVerify(exactly = 1) { repository.findByUsername(request.username) }
        verify(exactly = 1) { passwordHasher.verify(request.password, user.passwordHash) }
        coVerify(exactly = 1) { repository.revokeAllUserTokens(userId) }
        coVerify(exactly = 0) { tokenService.generateTokens(any(), any()) }
    }

    @Test
    fun shouldPropagateExceptionWhenGenerateTokensFailsInLogin() = runTest {
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
        every { passwordHasher.verify(request.password, user.passwordHash) } returns true
        coEvery { repository.revokeAllUserTokens(userId) } just runs
        coEvery { tokenService.generateTokens(userId, user.email) } throws Exception("Token generation failed")

        assertThrows<Exception> {
            authService.login(request)
        }

        coVerify(exactly = 1) { repository.revokeAllUserTokens(userId) }
        coVerify(exactly = 1) { tokenService.generateTokens(userId, user.email) }
    }

    @Test
    fun shouldPropagateExceptionWhenUpdatePasswordFailsInResetPassword() = runTest {
        val userId = UUID.randomUUID()
        val request = ResetPasswordRequest(
            token = "valid_reset_token",
            newPassword = "newPassword123"
        )
        val claims = PasswordResetTokenClaims(userId = userId, tokenId = UUID.randomUUID())
        val user = AuthModel(
            id = userId,
            username = "testuser",
            email = "user@example.com",
            displayName = "Test User",
            passwordHash = "oldHash"
        )
        val newPasswordHash = "newPasswordHash"

        coEvery { tokenService.consumePasswordResetToken(request.token) } returns claims
        coEvery { repository.findByUserId(userId) } returns user
        every { passwordHasher.hash(request.newPassword) } returns newPasswordHash
        coEvery { repository.updatePassword(userId, newPasswordHash) } throws Exception("Update failed")

        assertThrows<Exception> {
            authService.resetPassword(request)
        }

        coVerify(exactly = 1) { repository.updatePassword(userId, newPasswordHash) }
        coVerify(exactly = 0) { repository.revokeAllUserTokens(any()) }
        verify(exactly = 0) { emailService.sendPasswordChangedEmail(any(), any()) }
    }

    @Test
    fun shouldPropagateExceptionWhenRevokeAllUserTokensFailsInResetPassword() = runTest {
        val userId = UUID.randomUUID()
        val request = ResetPasswordRequest(
            token = "valid_reset_token",
            newPassword = "newPassword123"
        )
        val claims = PasswordResetTokenClaims(userId = userId, tokenId = UUID.randomUUID())
        val user = AuthModel(
            id = userId,
            username = "testuser",
            email = "user@example.com",
            displayName = "Test User",
            passwordHash = "oldHash"
        )
        val newPasswordHash = "newPasswordHash"

        coEvery { tokenService.consumePasswordResetToken(request.token) } returns claims
        coEvery { repository.findByUserId(userId) } returns user
        every { passwordHasher.hash(request.newPassword) } returns newPasswordHash
        coEvery { repository.updatePassword(userId, newPasswordHash) } just runs
        coEvery { repository.revokeAllUserTokens(userId) } throws Exception("Revocation failed")

        assertThrows<Exception> {
            authService.resetPassword(request)
        }

        coVerify(exactly = 1) { repository.updatePassword(userId, newPasswordHash) }
        coVerify(exactly = 1) { repository.revokeAllUserTokens(userId) }
        verify(exactly = 0) { emailService.sendPasswordChangedEmail(any(), any()) }
    }

    @Test
    fun shouldPropagateExceptionWhenUpdatePasswordFailsInChangePassword() = runTest {
        val userId = UUID.randomUUID()
        val request = ChangePasswordRequest(
            currentPassword = "oldPassword",
            newPassword = "newPassword123"
        )
        val user = AuthModel(
            id = userId,
            username = "testuser",
            email = "user@example.com",
            displayName = "Test User",
            passwordHash = "oldPasswordHash"
        )
        val newPasswordHash = "newPasswordHash"

        coEvery { repository.findByUserId(userId) } returns user
        every { passwordHasher.verify(request.currentPassword, user.passwordHash) } returns true
        every { passwordHasher.hash(request.newPassword) } returns newPasswordHash
        coEvery { repository.updatePassword(userId, newPasswordHash) } throws Exception("Update failed")

        assertThrows<Exception> {
            authService.changePassword(userId, request)
        }

        coVerify(exactly = 1) { repository.updatePassword(userId, newPasswordHash) }
        coVerify(exactly = 0) { repository.revokeAllUserTokens(any()) }
        coVerify(exactly = 0) { repository.deletePasswordResetTokensOfUser(any()) }
    }

    @Test
    fun shouldPropagateExceptionWhenRevokeAllUserTokensFailsInChangePassword() = runTest {
        val userId = UUID.randomUUID()
        val request = ChangePasswordRequest(
            currentPassword = "oldPassword",
            newPassword = "newPassword123"
        )
        val user = AuthModel(
            id = userId,
            username = "testuser",
            email = "user@example.com",
            displayName = "Test User",
            passwordHash = "oldPasswordHash"
        )
        val newPasswordHash = "newPasswordHash"

        coEvery { repository.findByUserId(userId) } returns user
        every { passwordHasher.verify(request.currentPassword, user.passwordHash) } returns true
        every { passwordHasher.hash(request.newPassword) } returns newPasswordHash
        coEvery { repository.updatePassword(userId, newPasswordHash) } just runs
        coEvery { repository.revokeAllUserTokens(userId) } throws Exception("Revocation failed")

        assertThrows<Exception> {
            authService.changePassword(userId, request)
        }

        coVerify(exactly = 1) { repository.updatePassword(userId, newPasswordHash) }
        coVerify(exactly = 1) { repository.revokeAllUserTokens(userId) }
        coVerify(exactly = 0) { repository.deletePasswordResetTokensOfUser(any()) }
    }

    @Test
    fun shouldPropagateExceptionWhenDeletePasswordResetTokensFailsInChangePassword() = runTest {
        val userId = UUID.randomUUID()
        val request = ChangePasswordRequest(
            currentPassword = "oldPassword",
            newPassword = "newPassword123"
        )
        val user = AuthModel(
            id = userId,
            username = "testuser",
            email = "user@example.com",
            displayName = "Test User",
            passwordHash = "oldPasswordHash"
        )
        val newPasswordHash = "newPasswordHash"

        coEvery { repository.findByUserId(userId) } returns user
        every { passwordHasher.verify(request.currentPassword, user.passwordHash) } returns true
        every { passwordHasher.hash(request.newPassword) } returns newPasswordHash
        coEvery { repository.updatePassword(userId, newPasswordHash) } just runs
        coEvery { repository.revokeAllUserTokens(userId) } just runs
        coEvery { repository.deletePasswordResetTokensOfUser(userId) } throws Exception("Deletion failed")

        assertThrows<Exception> {
            authService.changePassword(userId, request)
        }

        coVerify(exactly = 1) { repository.deletePasswordResetTokensOfUser(userId) }
        verify(exactly = 0) { emailService.sendPasswordChangedEmail(any(), any()) }
    }

    @Test
    fun shouldPropagateExceptionWhenDeleteUserFailsInDeleteAccount() = runTest {
        val userId = UUID.randomUUID()
        val request = DeleteAccountRequest(password = "correctPassword")
        val user = AuthModel(
            id = userId,
            username = "testuser",
            email = "user@example.com",
            displayName = "Test User",
            passwordHash = "passwordHash"
        )

        coEvery { repository.findByUserId(userId) } returns user
        every { passwordHasher.verify(request.password, user.passwordHash) } returns true
        coEvery { repository.deleteUser(userId) } throws Exception("Deletion failed")

        assertThrows<Exception> {
            authService.deleteAccount(userId, request)
        }

        coVerify(exactly = 1) { repository.deleteUser(userId) }
        verify(exactly = 0) { emailService.sendAccountDeletedEmail(any(), any()) }
    }
}