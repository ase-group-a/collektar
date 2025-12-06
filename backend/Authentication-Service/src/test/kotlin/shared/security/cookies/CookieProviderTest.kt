package com.collektar.shared.security.cookies

import com.collektar.shared.errors.AppError
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.mockk.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.assertThrows
import kotlin.test.assertEquals

class CookieProviderTest {
    private lateinit var call: ApplicationCall
    private lateinit var cookieProvider: ICookieProvider

    @BeforeEach
    fun setup() {
        call = mockk(relaxed = true)
        cookieProvider = CookieProvider(config = mockk(relaxed = true))
    }

    @Test
    fun `set appends cookie`() {
        cookieProvider.set(call, "TestCookie", "value", 3600, "/")

        verify { call.response.cookies.append(
            name = "TestCookie",
            value = "value",
            maxAge = 3600,
            path = "/",
            httpOnly = true,
            secure = any(),
            extensions = any()
        )}
    }

    @Test
    fun `get returns cookie value`() {
        val request = mockk<ApplicationRequest>()
        every { call.request } returns request
        every { request.cookies["TestCookie"] } returns "value"

        val result = cookieProvider.get(call, "TestCookie")
        assertEquals("value", result)
    }

    @Test
    fun `get throws Unauthorized when cookie missing`() {
        val request = mockk<ApplicationRequest>()
        every { call.request } returns request
        every { request.cookies["TestCookie"] } returns null

        assertThrows<AppError.Unauthorized.MissingToken> {
            cookieProvider.get(call, "TestCookie")
        }
    }

    @Test
    fun `delete appends deleted cookie`() {
        cookieProvider.delete(call, "TestCookie", "/")

        verify { call.response.cookies.append(
            name = "TestCookie",
            value = "deleted",
            maxAge = 0,
            path = "/",
            httpOnly = true,
            secure = any(),
            extensions = any()
        )}
    }

}
