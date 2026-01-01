package com.collektar.shared.errors

import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ErrorResponseTest {

    @Test
    fun `data class ErrorResponse stores message correctly`() {
        val message = "Something went wrong"
        val errorResponse = ErrorResponse(message)

        assertEquals(message, errorResponse.message)

        val same = ErrorResponse(message)
        val different = ErrorResponse("Different message")

        assertTrue(errorResponse == same)
        assertTrue(errorResponse != different)
        assertEquals(errorResponse.hashCode(), same.hashCode())

        val str = errorResponse.toString()
        assertTrue(str.contains(message))
    }
}