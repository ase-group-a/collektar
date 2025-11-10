package com.collektar.features.auth

import com.auth0.jwt.interfaces.DecodedJWT
import com.collektar.dto.*
import com.collektar.shared.errors.AppError
import com.collektar.shared.security.JWTService.IJWTService
import com.collektar.shared.security.JWTService.RefreshToken
import com.collektar.shared.security.PasswordHasher.IPasswordHasher
import java.time.Duration
import java.time.Instant
import java.util.*

class AuthService(
    private val repository: IAuthRepository,
    private val jwtService: IJWTService,
    private val passwordHasher: IPasswordHasher
) : IAuthService {
    override suspend fun register(request: RegisterRequest): RegisterResponse {
        if (repository.usernameExists(request.username)) {
            throw AppError.Conflict.UsernameTaken(request.username)
        }

        if (repository.emailExists(request.email)) {
            throw AppError.Conflict.EmailAlreadyInUse(request.email)
        }

        val userId = UUID.randomUUID()
        val passwordHash = passwordHasher.hash(request.password)
        val accessToken = jwtService.generateAccessToken(userId, request.email)
        val refreshToken = jwtService.generateRefreshToken(userId)
        val expiresIn: Long = Duration.between(Instant.now(), accessToken.expiresAt).toMillis()
        val refreshTokenExpiresIn: Long = Duration.between(Instant.now(), refreshToken.expiresAt).toMillis()

        repository.transaction {
            repository.createUser(
                userId = userId,
                username = request.username,
                email = request.email,
                displayName = request.displayName,
                passwordHash = passwordHash,
            )
            saveRefreshToken(userId = userId, token = refreshToken)
        }

        val userInfo = UserInfo(
            email = request.email,
            username = request.username,
            displayName = request.displayName
        )

        return RegisterResponse(
            accessToken = accessToken.token,
            expiresIn = expiresIn,
            refreshToken = refreshToken.token,
            refreshTokenExpiresIn = refreshTokenExpiresIn,
            user = userInfo
        )
    }

    override suspend fun login(request: LoginRequest): AuthenticationResponse {
        val user = repository.findByUsername(
            username = request.username
        ) ?: throw AppError.Unauthorized.InvalidCredentials()

        if (!passwordHasher.verify(password = request.password, user.passwordHash)) {
            throw AppError.Unauthorized.InvalidCredentials()
        }

        return authenticateUser(userId = user.id, email = user.email)
    }

    override suspend fun refresh(request: RefreshTokenRequest): AuthenticationResponse {
        val decodedJWT: DecodedJWT = jwtService.verify(
            token = request.refreshToken
        ) ?: throw AppError.Unauthorized.InvalidToken()

        val userId = UUID.fromString(decodedJWT.subject)
        val user = repository.findByUserId(
            userId = userId
        ) ?: throw AppError.Unauthorized.InvalidToken()

        return authenticateUser(userId = user.id, email = user.email)
    }

    private suspend fun authenticateUser(userId: UUID, email: String): AuthenticationResponse {
        val accessToken = jwtService.generateAccessToken(userId = userId, email = email)
        val expiresIn: Long = Duration.between(Instant.now(), accessToken.expiresAt).toMillis()
        val refreshToken = jwtService.generateRefreshToken(userId = userId)
        val refreshTokenExpiresIn: Long = Duration.between(Instant.now(), refreshToken.expiresAt).toMillis()

        saveRefreshToken(userId = userId, token = refreshToken)

        return AuthenticationResponse(
            accessToken = accessToken.token,
            expiresIn = expiresIn,
            refreshToken = refreshToken.token,
            refreshTokenExpiresIn = refreshTokenExpiresIn
        )
    }

    private suspend fun saveRefreshToken(userId: UUID, token: RefreshToken) {
        repository.saveRefreshToken(
            userId = userId,
            token = token.token,
            expiresAt = token.expiresAt,
            issuedAt = token.issuedAt
        )
    }
}