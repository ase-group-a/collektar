package com.collektar.features.collection.service

import com.collektar.dto.CollectionInfo
import com.collektar.dto.CollectionItemInfo
import com.collektar.features.collection.repository.CollectionRepository
import io.mockk.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.util.*
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class CollectionServiceTest {

    private lateinit var repository: CollectionRepository
    private lateinit var service: CollectionService
    private val userId = UUID.randomUUID()

    @BeforeEach
    fun setup() {
        repository = mockk()
        service = CollectionService(repository)
    }

    @Test
    fun `ensureDefaults calls repository`() {
        every { repository.ensureDefaultCollections(userId) } just Runs

        service.ensureDefaults(userId)

        verify { repository.ensureDefaultCollections(userId) }
    }

    @Test
    fun `listCollections returns repository result`() {
        val collections = listOf(CollectionInfo("1", "GAMES", false, 0L))
        every { repository.getCollectionsForUser(userId) } returns collections

        val result = service.listCollections(userId)

        assertEquals(collections, result)
        verify { repository.getCollectionsForUser(userId) }
    }

    @Test
    fun `setVisibility updates repository or throws`() {
        every { repository.setVisibility(userId, "GAMES", true) } returns true
        service.setVisibility(userId, "GAMES", true)
        verify { repository.setVisibility(userId, "GAMES", true) }

        every { repository.setVisibility(userId, "MOVIES", false) } returns false

        assertFailsWith<NoSuchElementException> {
            service.setVisibility(userId, "MOVIES", false)
        }
    }

    @Test
    fun `addItem adds item or throws`() {
        val collectionId = UUID.randomUUID()
        every { repository.findCollectionId(userId, "GAMES") } returns collectionId
        every { repository.addItemToCollection(collectionId, "item123", "Title", null, null, null) } returns UUID.randomUUID()

        val item = service.addItem(userId, "GAMES", "item123", "Title", null, null, null)

        assertEquals("item123", item.itemId)

        verify { repository.findCollectionId(userId, "GAMES") }
        verify { repository.addItemToCollection(collectionId, "item123", "Title", null, null, null) }

        every { repository.findCollectionId(userId, "MOVIES") } returns null

        assertFailsWith<NoSuchElementException> {
            service.addItem(userId, "MOVIES", "id", null, null, null, null)
        }
    }

    @Test
    fun `listItems returns items or throws`() {
        val collectionId = UUID.randomUUID()
        val items = listOf(CollectionItemInfo("1", "item1", "Title", null, null, null, 0L))
        every { repository.findCollectionId(userId, "GAMES") } returns collectionId
        every { repository.getItemsForCollection(collectionId) } returns items

        val result = service.listItems(userId, "GAMES")

        assertEquals(items, result)

        every { repository.findCollectionId(userId, "MOVIES") } returns null

        assertFailsWith<NoSuchElementException> {
            service.listItems(userId, "MOVIES")
        }
    }

    @Test
    fun `removeItem removes item or throws`() {
        val collectionId = UUID.randomUUID()
        every { repository.findCollectionId(userId, "GAMES") } returns collectionId
        every { repository.removeItemFromCollection(collectionId, "item123") } returns true

        service.removeItem(userId, "GAMES", "item123")
        verify { repository.removeItemFromCollection(collectionId, "item123") }

        every { repository.removeItemFromCollection(collectionId, "item456") } returns false

        assertFailsWith<NoSuchElementException> {
            service.removeItem(userId, "GAMES", "item456")
        }

        every { repository.findCollectionId(userId, "MOVIES") } returns null

        assertFailsWith<NoSuchElementException> {
            service.removeItem(userId, "MOVIES", "item")
        }
    }

    @Test
    fun `createCollection returns new collection`() {
        val newId = UUID.randomUUID()
        every { repository.createCollection(userId, "MOVIES") } returns newId

        val result = service.createCollection(userId, "MOVIES")

        assertEquals("MOVIES", result.type)

        verify { repository.createCollection(userId, "MOVIES") }
    }

    @Test
    fun `deleteCollection removes collection or throws`() {
        every { repository.deleteCollection(userId, "1") } returns true

        assertEquals(true, service.deleteCollection(userId, "1"))

        every { repository.deleteCollection(userId, "2") } returns false

        assertFailsWith<NoSuchElementException> {
            service.deleteCollection(userId, "2")
        }
    }
}