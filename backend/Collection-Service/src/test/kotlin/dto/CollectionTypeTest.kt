package com.collektar.dto

import com.collektar.features.collection.model.CollectionType
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test

class CollectionTypeTest {

    @Test
    fun `fromString returns enum when value is valid uppercase`() {
        val result = CollectionType.fromString("GAMES")
        assertEquals(CollectionType.GAMES, result)
    }

    @Test
    fun `fromString returns enum when value is lowercase`() {
        val result = CollectionType.fromString("movies")
        assertEquals(CollectionType.MOVIES, result)
    }

    @Test
    fun `fromString returns null when value is invalid`() {
        val result = CollectionType.fromString("invalid")
        assertNull(result)
    }

    @Test
    fun `fromString returns null when value is empty`() {
        val result = CollectionType.fromString("")
        assertNull(result)
    }
}
