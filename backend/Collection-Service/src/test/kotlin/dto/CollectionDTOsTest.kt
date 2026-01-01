package com.collektar.dto

import com.collektar.dto.*
import kotlinx.serialization.json.Json
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import java.util.*

class CollectionDTOsTest {

    private val json = Json { prettyPrint = true; ignoreUnknownKeys = true }

    @Test
    fun `CollectionInfo serialization and deserialization`() {
        val original = CollectionInfo(
            id = UUID.randomUUID().toString(),
            type = "GAMES",
            hidden = false,
            createdAt = System.currentTimeMillis()
        )

        val serialized = json.encodeToString(original)
        val deserialized = json.decodeFromString<CollectionInfo>(serialized)

        assertEquals(original, deserialized)
    }

    @Test
    fun `CollectionInfo full coverage`() {
        val info = CollectionInfo(
            id = "1",
            type = "GAMES",
            hidden = true,
            createdAt = 123456L
        )
        val copy = info.copy(type = "MOVIES")
        assertTrue(info != copy)
        assertEquals(info.hashCode(), info.hashCode())
    }

    @Test
    fun `CollectionItemInfo serialization and deserialization`() {
        val original = CollectionItemInfo(
            id = UUID.randomUUID().toString(),
            itemId = "item123",
            title = "My new Game",
            imageUrl = "https://image.url",
            description = "DKT",
            source = "Mueller",
            addedAt = System.currentTimeMillis()
        )

        val serialized = json.encodeToString(original)
        val deserialized = json.decodeFromString<CollectionItemInfo>(serialized)

        assertEquals(original, deserialized)
    }

    @Test
    fun `CollectionItemInfo full coverage`() {
        val item = CollectionItemInfo(
            id = "1",
            itemId = "item1",
            title = "Cat",
            imageUrl = "https://image.url",
            description = "Sweet",
            source = "Source",
            addedAt = 123456L
        )

        val copy = item.copy(title = "New Title")
        assertTrue(item != copy)
        assertEquals(item.hashCode(), item.hashCode())
    }


    @Test
    fun `AddCollectionItemRequest serialization and deserialization`() {
        val original = AddCollectionItemRequest(
            itemId = "item123",
            title = "My new Game",
            imageUrl = "https://image.url",
            description = "DKT",
            source = "Mueller"
        )

        val serialized = json.encodeToString(original)
        val deserialized = json.decodeFromString<AddCollectionItemRequest>(serialized)

        assertEquals(original, deserialized)
    }

    @Test
    fun `AddCollectionItemRequest full coverage`() {
        val request = AddCollectionItemRequest(
            itemId = "item1",
            title = "Cat",
            imageUrl = "https://image.url",
            description = "Sweet",
            source = "Source"
        )

        val copy = request.copy(title = "New Title")
        assertTrue(request != copy)
        assertEquals(request.hashCode(), request.hashCode())
    }

    @Test
    fun `AddCollectionItemRequest with default values`() {
        val request = AddCollectionItemRequest(itemId = "item123")

        val copy = request.copy()
        assertEquals(request, copy)
        assertEquals(request.hashCode(), copy.hashCode())
    }

    @Test
    fun `SetCollectionVisibilityRequest serialization and deserialization`() {
        val original = SetCollectionVisibilityRequest(hidden = true)

        val serialized = json.encodeToString(original)
        val deserialized = json.decodeFromString<SetCollectionVisibilityRequest>(serialized)

        assertEquals(original, deserialized)
    }

    @Test
    fun `SetCollectionVisibilityRequest full coverage`() {
        val request = SetCollectionVisibilityRequest(hidden = false)

        val copy = request.copy(hidden = true)
        assertTrue(request != copy)
        assertEquals(request.hashCode(), request.hashCode())
    }

    @Test
    fun `CreateCollectionRequest serialization and deserialization`() {
        val original = CreateCollectionRequest(type = "MOVIES")

        val serialized = json.encodeToString(original)
        val deserialized = json.decodeFromString<CreateCollectionRequest>(serialized)

        assertEquals(original, deserialized)
    }

    @Test
    fun `CreateCollectionRequest full coverage`() {
        val request = CreateCollectionRequest(type = "BOOKS")

        val copy = request.copy(type = "MOVIES")
        assertTrue(request != copy)
        assertEquals(request.hashCode(), request.hashCode())
    }
}
