package com.collektar.utility

import com.collektar.shared.errors.AppError
import com.collektar.shared.utility.userId
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.util.*
import kotlin.test.assertEquals

class ApplicationExtensionUserId {
    private lateinit var mockCall: ApplicationCall
    private lateinit var mockRequest: ApplicationRequest
    private lateinit var mockHeaders: Headers

    @BeforeEach
    fun setup() {
        mockCall = mockk()
        mockRequest = mockk()
        mockHeaders = mockk()

        every { mockCall.request } returns mockRequest
        every { mockRequest.headers } returns mockHeaders
    }

    @Test
    fun shouldReturnUserIdWhenValidUUIDInHeader() {
        val expectedUserId = UUID.randomUUID()

        every { mockHeaders["X-User-Id"] } returns expectedUserId.toString()

        val result = mockCall.userId

        assertEquals(expectedUserId, result)
    }

    @Test
    fun shouldThrowMissingUserIdWhenHeaderIsNull() {
        every { mockHeaders["X-User-Id"] } returns null

        assertThrows<AppError.Unauthorized.MissingUserId> {
            mockCall.userId
        }
    }

    @Test
    fun shouldThrowInvalidUserIdFormatWhenHeaderIsNotValidUUID() {
        every { mockHeaders["X-User-Id"] } returns "not-a-valid-uuid"

        assertThrows<AppError.BadRequest.InvalidUserIdFormat> {
            mockCall.userId
        }
    }
}