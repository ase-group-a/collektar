package com.collektar.features.auth

import com.collektar.dto.*
import com.collektar.features.auth.service.IAuthService
import com.collektar.shared.errors.AppError
import com.collektar.shared.security.cookies.ICookieProvider
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.plugins.statuspages.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.testing.*
import io.mockk.*
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonNamingStrategy
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.util.*

class AuthRoutesTest {
    private lateinit var authService: IAuthService
    private lateinit var cookieProvider: ICookieProvider


    @BeforeEach
    fun setUp() {
        authService = mockk(relaxed = true)
        cookieProvider = mockk(relaxed = true)
    }

    @AfterEach
    fun tearDown() {
        clearAllMocks()
    }

    @OptIn(ExperimentalSerializationApi::class)
    private fun ApplicationTestBuilder.jsonClient() = createClient {
        install(io.ktor.client.plugins.contentnegotiation.ContentNegotiation) {
            json(Json {
                prettyPrint = true
                isLenient = true
                ignoreUnknownKeys = true
                namingStrategy = JsonNamingStrategy.SnakeCase
            })
        }
    }

    @Test
    fun shouldReturnCreatedAndTokensIfValidRegisterRequest() = testApplication {
        val client = jsonClient()
        val request = RegisterRequest(
            username = "newuser",
            email = "new@test.com",
            displayName = "New User",
            password = "SecurePass123!"
        )
        val response = AuthenticationResponse(
            refreshToken = "refresh_token",
            refreshTokenExpiresIn = 86400,
            accessTokenResponse = AccessTokenResponse(
                accessToken = "access_token",
                expiresIn = 3600,
                user = UserInfo(
                    email = request.email,
                    username = request.username,
                    displayName = request.displayName
                )
            )
        )

        coEvery { authService.register(any()) } returns response

        application {
            configureTestRouting()
        }

        val result = client.post("/register") {
            contentType(ContentType.Application.Json)
            setBody(request)
        }

        assertEquals(HttpStatusCode.Created, result.status)
        coVerify(exactly = 1) { authService.register(any()) }
    }

    @Test
    fun shouldReturnBadRequestIfUsernameInvalid() = testApplication {
        application { configureTestRouting() }
        val client = jsonClient()

        val request = RegisterRequest(
            username = "ab",
            email = "test@test.com",
            displayName = "Test",
            password = "Password123!"
        )

        val result = client.post("/register") {
            contentType(ContentType.Application.Json)
            setBody(request)
        }

        assertEquals(HttpStatusCode.BadRequest, result.status)
    }

    @Test
    fun shouldReturnBadRequestIfEmailIsInvalid() = testApplication {
        application { configureTestRouting() }
        val client = jsonClient()

        val request = RegisterRequest(
            username = "validuser",
            email = "invalid-email",
            displayName = "Test",
            password = "Password123!"
        )

        val result = client.post("/register") {
            contentType(ContentType.Application.Json)
            setBody(request)
        }

        assertEquals(HttpStatusCode.BadRequest, result.status)
    }

    @Test
    fun shouldReturnBadRequestIfPasswordInvalid() = testApplication {
        application { configureTestRouting() }
        val client = jsonClient()

        val request = RegisterRequest(
            username = "validuser",
            email = "valid@test.com",
            displayName = "Test",
            password = "weak"
        )

        val result = client.post("/register") {
            contentType(ContentType.Application.Json)
            setBody(request)
        }

        assertEquals(HttpStatusCode.BadRequest, result.status)
    }

    @Test
    fun shouldReturnBadRequestIfDisplayNameInvalid() = testApplication {
        application { configureTestRouting() }
        val client = jsonClient()

        val request = RegisterRequest(
            username = "validuser",
            email = "valid@test.com",
            displayName = "",
            password = "ValidPass123!"
        )

        val result = client.post("/register") {
            contentType(ContentType.Application.Json)
            setBody(request)
        }

        assertEquals(HttpStatusCode.BadRequest, result.status)
    }

    @Test
    fun shouldReturnOKIfValidCredentials() = testApplication {
        val response = AuthenticationResponse(
            refreshToken = "refresh_token",
            refreshTokenExpiresIn = 86400,
            accessTokenResponse = AccessTokenResponse(
                accessToken = "access_token",
                expiresIn = 3600,
                user = UserInfo(
                    email = "testuser@mail.com",
                    username = "testuser",
                    displayName = "Test User"
                )
            )
        )

        coEvery { authService.login(any()) } returns response

        application { configureTestRouting() }
        val client = jsonClient()

        val request = LoginRequest(
            username = "testuser",
            password = "Password123!"
        )

        val result = client.post("/login") {
            contentType(ContentType.Application.Json)
            setBody(request)
        }

        assertEquals(HttpStatusCode.OK, result.status)
        coVerify(exactly = 1) { authService.login(any()) }
    }

    @Test
    fun shouldReturnUnauthorizedIfCredentialsInvalid() = testApplication {
        coEvery { authService.login(any()) } throws AppError.Unauthorized.InvalidCredentials()

        application { configureTestRouting() }
        val client = jsonClient()

        val request = LoginRequest("testuser", "wrongpassword")

        val result = client.post("/login") {
            contentType(ContentType.Application.Json)
            setBody(request)
        }

        assertEquals(HttpStatusCode.Unauthorized, result.status)
    }

    @Test
    fun shouldReturnOKAndNewTokensIfValid() = testApplication {
        val response = AuthenticationResponse(
            refreshToken = "refresh_token",
            refreshTokenExpiresIn = 86400,
            accessTokenResponse = AccessTokenResponse(
                accessToken = "access_token",
                expiresIn = 3600,
                user = UserInfo(
                    email = "testuser@mail.com",
                    username = "testuser",
                    displayName = "Test User"
                )
            )
        )

        coEvery { authService.refresh(any()) } returns response
        every { cookieProvider.get(any(), "refresh_token") } returns "valid_refresh_token"

        application { configureTestRouting() }
        val client = jsonClient()

        val result = client.post("/refresh") {
        }

        assertEquals(HttpStatusCode.OK, result.status)
        coVerify(exactly = 1) { authService.refresh(any()) }
    }

    @Test
    fun shouldReturnBadRequestIfRefreshTokenIsBlank() = testApplication {
        application { configureTestRouting() }
        val client = jsonClient()

        val request = RefreshTokenRequest(refreshToken = "")

        val result = client.post("/refresh") {
            contentType(ContentType.Application.Json)
            setBody(request)
        }

        assertEquals(HttpStatusCode.BadRequest, result.status)
        coVerify(exactly = 0) { authService.refresh(any()) }
    }

    @Test
    fun shouldReturnUnauthorizedIfTokenInvalid() = testApplication {
        coEvery { authService.refresh(any()) } throws AppError.Unauthorized.InvalidToken()
        every { cookieProvider.get(any(), "refresh_token") } returns "invalid_token"

        application { configureTestRouting() }
        val client = jsonClient()

        val result = client.post("/refresh") {
        }

        assertEquals(HttpStatusCode.Unauthorized, result.status)
    }

    @Test
    fun shouldReturnOKIfTokenValid() = testApplication {
        coEvery { authService.verify(any(), any()) } just runs

        application { configureTestRouting() }
        val client = jsonClient()

        val result = client.get("/verify") {
            header("Authorization", "Bearer valid_token")
        }

        assertEquals(HttpStatusCode.OK, result.status)
        coVerify(exactly = 1) { authService.verify("valid_token", any()) }
    }

    @Test
    fun shouldReturnUnauthorizedIfAuthHeaderMissing() = testApplication {
        application { configureTestRouting() }
        val client = jsonClient()

        val result = client.get("/verify")

        assertEquals(HttpStatusCode.Unauthorized, result.status)
    }

    @Test
    fun shouldReturnUnauthorizedIfTokenDoesntStartWithBearer() = testApplication {
        application { configureTestRouting() }
        val client = jsonClient()

        val result = client.get("/verify") {
            header("Authorization", "Basic token")
        }

        assertEquals(HttpStatusCode.Unauthorized, result.status)
    }

    @Test
    fun shouldReturnUnauthorizedIfTokenIsBlankAfterBearerPrefix() = testApplication {
        application { configureTestRouting() }
        val client = jsonClient()

        val result = client.get("/verify") {
            header("Authorization", "Bearer ")
        }

        assertEquals(HttpStatusCode.Unauthorized, result.status)
    }

    @Test
    fun shouldReturnUnauthorizedIfTokenInvalidInVerify() = testApplication {
        coEvery { authService.verify(any(), any()) } throws AppError.Unauthorized.InvalidToken()

        application { configureTestRouting() }
        val client = jsonClient()

        val result = client.get("/verify") {
            header("Authorization", "Bearer invalid_token")
        }

        assertEquals(HttpStatusCode.Unauthorized, result.status)
    }

    @OptIn(ExperimentalSerializationApi::class)
    private fun Application.configureTestRouting() {
        install(io.ktor.server.plugins.contentnegotiation.ContentNegotiation) {
            json(Json {
                prettyPrint = true
                isLenient = true
                ignoreUnknownKeys = true
                namingStrategy = JsonNamingStrategy.SnakeCase
            })
        }
        install(StatusPages) {
            exception<AppError> { call, cause ->
                call.respond(
                    cause.statusCode,
                    ErrorResponse(message = cause.message ?: "Unexpected error occurred.")
                )
            }
            exception<Throwable> { call, cause ->
                call.application.log.error("Unhandled exception", cause)
                call.respond(HttpStatusCode.InternalServerError, ErrorResponse("Internal Server Error"))
            }
        }
        routing {
            authRoutes(
                authService,
                cookieProvider
            )
        }
    }

    @Test
    fun shouldLogoutSuccessfully() = testApplication {
        every { cookieProvider.get(any(), "refresh_token") } returns "refresh_token_value"
        coEvery { authService.logout("refresh_token_value") } just runs
        every { cookieProvider.delete(any(), "refresh_token") } just runs

        application { configureTestRouting() }
        val client = jsonClient()

        val result = client.post("/logout")

        assertEquals(HttpStatusCode.OK, result.status)

        coVerify(exactly = 1) { authService.logout("refresh_token_value") }
        verify(exactly = 1) { cookieProvider.get(any(), "refresh_token") }
        verify(exactly = 1) { cookieProvider.delete(any(), "refresh_token") }
    }

    @Test
    fun shouldReturnOKForValidForgotPasswordRequest() = testApplication {
        coEvery { authService.forgotPassword(any()) } just runs

        application { configureTestRouting() }
        val client = jsonClient()

        val request = ForgotPasswordRequest(email = "user@example.com")

        val result = client.post("/forgot-password") {
            contentType(ContentType.Application.Json)
            setBody(request)
        }

        assertEquals(HttpStatusCode.OK, result.status)
        coVerify(exactly = 1) { authService.forgotPassword(any()) }
    }

    @Test
    fun shouldReturnBadRequestForInvalidEmailInForgotPassword() = testApplication {
        application { configureTestRouting() }
        val client = jsonClient()

        val request = ForgotPasswordRequest(email = "invalid-email")

        val result = client.post("/forgot-password") {
            contentType(ContentType.Application.Json)
            setBody(request)
        }

        assertEquals(HttpStatusCode.BadRequest, result.status)
        coVerify(exactly = 0) { authService.forgotPassword(any()) }
    }

    @Test
    fun shouldReturnOKForValidResetPasswordRequest() = testApplication {
        coEvery { authService.resetPassword(any()) } just runs

        application { configureTestRouting() }
        val client = jsonClient()

        val request = ResetPasswordRequest(
            token = "valid_reset_token",
            newPassword = "NewPassword123!"
        )

        val result = client.post("/reset-password") {
            contentType(ContentType.Application.Json)
            setBody(request)
        }

        assertEquals(HttpStatusCode.OK, result.status)
        coVerify(exactly = 1) { authService.resetPassword(any()) }
    }

    @Test
    fun shouldReturnBadRequestForInvalidPasswordInResetPassword() = testApplication {
        application { configureTestRouting() }
        val client = jsonClient()

        val request = ResetPasswordRequest(
            token = "valid_reset_token",
            newPassword = "weak"
        )

        val result = client.post("/reset-password") {
            contentType(ContentType.Application.Json)
            setBody(request)
        }

        assertEquals(HttpStatusCode.BadRequest, result.status)
        coVerify(exactly = 0) { authService.resetPassword(any()) }
    }

    @Test
    fun shouldReturnOKForValidChangePasswordRequest() = testApplication {
        coEvery { authService.changePassword(any(), any()) } just runs
        every { cookieProvider.delete(any(), "refresh_token") } just runs

        application { configureTestRouting() }
        val client = jsonClient()

        val request = ChangePasswordRequest(
            currentPassword = "OldPassword123!",
            newPassword = "NewPassword123!"
        )

        val result = client.post("/change-password") {
            contentType(ContentType.Application.Json)
            header("X-User-Id", UUID.randomUUID().toString())
            setBody(request)
        }

        assertEquals(HttpStatusCode.OK, result.status)
        coVerify(exactly = 1) { authService.changePassword(any(), any()) }
        verify(exactly = 1) { cookieProvider.delete(any(), "refresh_token") }
    }

    @Test
    fun shouldReturnBadRequestForInvalidNewPasswordInChangePassword() = testApplication {
        application { configureTestRouting() }
        val client = jsonClient()

        val request = ChangePasswordRequest(
            currentPassword = "OldPassword123!",
            newPassword = "weak"
        )

        val result = client.post("/change-password") {
            contentType(ContentType.Application.Json)
            header("X-User-Id", UUID.randomUUID().toString())
            setBody(request)
        }

        assertEquals(HttpStatusCode.BadRequest, result.status)
        coVerify(exactly = 0) { authService.changePassword(any(), any()) }
    }

    @Test
    fun shouldReturnUnauthorizedForMissingUserIdInChangePassword() = testApplication {
        application { configureTestRouting() }
        val client = jsonClient()

        val request = ChangePasswordRequest(
            currentPassword = "OldPassword123!",
            newPassword = "NewPassword123!"
        )

        val result = client.post("/change-password") {
            contentType(ContentType.Application.Json)
            setBody(request)
        }

        assertEquals(HttpStatusCode.Unauthorized, result.status)
        coVerify(exactly = 0) { authService.changePassword(any(), any()) }
    }

    @Test
    fun shouldReturnOKForValidDeleteAccountRequest() = testApplication {
        coEvery { authService.deleteAccount(any(), any()) } just runs
        every { cookieProvider.delete(any(), "refresh_token") } just runs

        application { configureTestRouting() }
        val client = jsonClient()

        val request = DeleteAccountRequest(password = "Password123!")

        val result = client.delete("/account") {
            contentType(ContentType.Application.Json)
            header("X-User-Id", UUID.randomUUID().toString())
            setBody(request)
        }

        assertEquals(HttpStatusCode.OK, result.status)
        coVerify(exactly = 1) { authService.deleteAccount(any(), any()) }
        verify(exactly = 1) { cookieProvider.delete(any(), "refresh_token") }
    }

    @Test
    fun shouldReturnUnauthorizedForMissingUserIdInDeleteAccount() = testApplication {
        application { configureTestRouting() }
        val client = jsonClient()

        val request = DeleteAccountRequest(password = "Password123!")

        val result = client.delete("/account") {
            contentType(ContentType.Application.Json)
            setBody(request)
        }

        assertEquals(HttpStatusCode.Unauthorized, result.status)
        coVerify(exactly = 0) { authService.deleteAccount(any(), any()) }
    }
}