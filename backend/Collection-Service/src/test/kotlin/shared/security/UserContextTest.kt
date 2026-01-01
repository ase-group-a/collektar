package com.collektar.shared.security

import io.ktor.server.application.ApplicationCall
import io.ktor.server.request.*
import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.Test
import java.util.*
import kotlin.test.assertEquals
import kotlin.test.assertNull

class UserContextTest {

    @Test
    fun `userIdFromHeader returns UUID when header is valid`() {
        val call = mockk<ApplicationCall>()
        val request = mockk<ApplicationRequest>()
        val uuid = UUID.randomUUID()

        every { call.request } returns request
        every { request.header("X-User-Id") } returns uuid.toString()

        val result = UserContext.userIdFromHeader(call, "X-User-Id")
        assertEquals(uuid, result)
    }

    @Test
    fun `userIdFromHeader returns null when header is missing`() {
        val call = mockk<ApplicationCall>()
        val request = mockk<ApplicationRequest>()

        every { call.request } returns request
        every { request.header("X-User-Id") } returns null

        val result = UserContext.userIdFromHeader(call, "X-User-Id")
        assertNull(result)
    }

    @Test
    fun `userIdFromHeader returns null when header is invalid UUID`() {
        val call = mockk<ApplicationCall>()
        val request = mockk<ApplicationRequest>()

        every { call.request } returns request
        every { request.header("X-User-Id") } returns "not-a-uuid"

        val result = UserContext.userIdFromHeader(call, "X-User-Id")
        assertNull(result)
    }
}