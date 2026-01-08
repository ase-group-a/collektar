package com.collektar.features.auth.service

import com.collektar.dto.*
import com.collektar.features.auth.repository.IAuthRepository
import com.collektar.shared.email.IEmailService
import com.collektar.shared.errors.AppError
import com.collektar.shared.security.passwordhasher.IPasswordHasher
import com.collektar.shared.security.tokenservice.ITokenService
import com.collektar.shared.security.tokenservice.TokenPair
import io.ktor.server.routing.*
import java.util.*

class AuthService(
    private val repository: IAuthRepository,
    private val tokenService: ITokenService,
    private val passwordHasher: IPasswordHasher,
    private val emailService: IEmailService
) : IAuthService {
    override suspend fun register(request: RegisterRequest): AuthenticationResponse {
        if (repository.usernameExists(request.username)) {
            throw AppError.Conflict.UsernameTaken(request.username)
        }

        if (repository.emailExists(request.email)) {
            throw AppError.Conflict.EmailAlreadyInUse()
        }

        val userId = UUID.randomUUID()
        val passwordHash = passwordHasher.hash(request.password)
        repository.createUser(
            userId = userId,
            username = request.username,
            email = request.email,
            displayName = request.displayName,
            passwordHash = passwordHash,
        )

        val tokenPair = tokenService.generateTokens(userId = userId, email = request.email)

        val userInfo = UserInfo(
            email = request.email,
            username = request.username,
            displayName = request.displayName
        )

        emailService.sendWelcomeEmail(to = request.email, displayName = request.displayName)

        return AuthenticationResponse(
            refreshToken = tokenPair.refreshToken,
            refreshTokenExpiresIn = tokenPair.refreshTokenExpiresIn,
            accessTokenResponse = AccessTokenResponse(
                accessToken = tokenPair.accessToken,
                expiresIn = tokenPair.accessTokenExpiresIn,
                user = userInfo
            )
        )
    }

    override suspend fun login(request: LoginRequest): AuthenticationResponse {
        val user = repository.findByUsername(
            username = request.username
        ) ?: throw AppError.Unauthorized.InvalidCredentials()

        if (!passwordHasher.verify(password = request.password, user.passwordHash)) {
            throw AppError.Unauthorized.InvalidCredentials()
        }

        repository.revokeAllUserTokens(user.id)
        val tokenPair: TokenPair = tokenService.generateTokens(userId = user.id, email = user.email)

        val userInfo = UserInfo(
            email = user.email,
            username = user.username,
            displayName = user.displayName
        )

        return AuthenticationResponse(
            refreshToken = tokenPair.refreshToken,
            refreshTokenExpiresIn = tokenPair.refreshTokenExpiresIn,
            accessTokenResponse = AccessTokenResponse(
                accessToken = tokenPair.accessToken,
                expiresIn = tokenPair.accessTokenExpiresIn,
                user = userInfo
            )
        )
    }

    override suspend fun refresh(request: RefreshTokenRequest): AuthenticationResponse {
        val tokenPair = tokenService.validateAndRefresh(request.refreshToken)
        val claims = tokenService.validateAccessToken(tokenPair.accessToken)

        val user = repository.findByUserId(
            userId = claims.userId
        ) ?: throw AppError.Unauthorized.InvalidCredentials()

        val userInfo = UserInfo(
            email = user.email,
            username = user.username,
            displayName = user.displayName
        )

        return AuthenticationResponse(
            refreshToken = tokenPair.refreshToken,
            refreshTokenExpiresIn = tokenPair.refreshTokenExpiresIn,
            accessTokenResponse = AccessTokenResponse(
                accessToken = tokenPair.accessToken,
                expiresIn = tokenPair.accessTokenExpiresIn,
                user = userInfo
            )
        )
    }

    override suspend fun verify(token: String, routingCall: RoutingCall) {
        val claims = tokenService.validateAccessToken(token)
        routingCall.response.headers.append("X-User-Id", claims.userId.toString())
        routingCall.response.headers.append("X-User-Email", claims.email)
    }

    override suspend fun logout(token: String) {
        tokenService.revokeRefreshToken(token)
    }

    override suspend fun forgotPassword(request: ForgotPasswordRequest) {
        val user = repository.findByEmail(request.email) ?: return

        val passwordResetToken = tokenService.generatePasswordResetToken(user.id)

        emailService.sendPasswordResetEmail(
            to = user.email,
            displayName = user.displayName,
            resetToken = passwordResetToken.token,
            expiryMinutes = passwordResetToken.validityMinutes
        )
    }

    override suspend fun resetPassword(request: ResetPasswordRequest) {
        val claims = tokenService.consumePasswordResetToken(request.token)
        val user = repository.findByUserId(claims.userId) ?: throw AppError.NotFound.UserNotFound()
        val newPasswordHash = passwordHasher.hash(request.newPassword)

        repository.updatePassword(claims.userId, newPasswordHash)
        repository.revokeAllUserTokens(claims.userId)

        emailService.sendPasswordChangedEmail(
            to = user.email,
            displayName = user.displayName
        )
    }

    override suspend fun changePassword(userId: UUID, request: ChangePasswordRequest) {
        val user = repository.findByUserId(userId) ?: throw AppError.NotFound.UserNotFound()

        if (!passwordHasher.verify(request.currentPassword, user.passwordHash)) {
            throw AppError.Unauthorized.InvalidCredentials()
        }

        val newPasswordHash = passwordHasher.hash(request.newPassword)
        repository.updatePassword(userId, newPasswordHash)
        repository.revokeAllUserTokens(userId)
        repository.deletePasswordResetTokensOfUser(userId)
        emailService.sendPasswordChangedEmail(
            to = user.email,
            displayName = user.displayName
        )
    }

    override suspend fun deleteAccount(userId: UUID, request: DeleteAccountRequest) {
        val user = repository.findByUserId(userId) ?: throw AppError.NotFound.UserNotFound()
        if (!passwordHasher.verify(request.password, user.passwordHash)) {
            throw AppError.Unauthorized.InvalidCredentials()
        }

        val email = user.email
        val displayName = user.displayName

        repository.deleteUser(userId)

        emailService.sendAccountDeletedEmail(
            to = email,
            displayName = displayName
        )
    }
}
