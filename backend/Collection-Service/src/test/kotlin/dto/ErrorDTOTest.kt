package com.collektar.dto

import kotlinx.serialization.json.Json
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

class ErrorDTOTest {

    private val json = Json { prettyPrint = true; ignoreUnknownKeys = true }

    @Test
    fun `ErrorResponse serialization and deserialization`() {
        val original = ErrorResponse(message = "An error occurred")

        val serialized = json.encodeToString(original)
        val deserialized = json.decodeFromString<ErrorResponse>(serialized)

        assertEquals(original, deserialized)
    }

    @Test
    fun `ErrorResponse full coverage`() {
        val error = ErrorResponse(message = "Test message")

        val copy = error.copy(message = "New message")
        assertTrue(error != copy)
        assertEquals(error.hashCode(), error.hashCode())

        assertEquals(error, error)
    }
}