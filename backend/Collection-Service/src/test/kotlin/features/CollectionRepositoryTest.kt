package com.collektar.features

import com.collektar.features.collection.repository.CollectionRepository
import com.collektar.shared.database.Tables
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.exposed.sql.deleteAll
import org.junit.jupiter.api.*
import java.util.*
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlin.test.assertFalse

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class CollectionRepositoryTest {

    private val repo = CollectionRepository()
    private val userId = UUID.randomUUID()

    @BeforeAll
    fun setupDatabase() {
        Database.connect("jdbc:h2:mem:test;DB_CLOSE_DELAY=-1;", driver = "org.h2.Driver")

        transaction {
            SchemaUtils.create(Tables.Collections, Tables.CollectionItems)
        }
    }

    @BeforeEach
    fun cleanTables() {
        transaction {
            Tables.CollectionItems.deleteAll()
            Tables.Collections.deleteAll()
        }
    }

    @Test
    fun `ensureDefaultCollections creates missing collections`() {
        repo.ensureDefaultCollections(userId)
        val collections = repo.getCollectionsForUser(userId)

        assertEquals(6, collections.size)

        val types = collections.map { it.type }.toSet()

        assertTrue(setOf("GAMES", "MOVIES", "SHOWS", "BOOKS", "MUSIC", "BOARDGAMES").all { it in types })
    }

    @Test
    fun `getCollectionsForUser returns ordered collections`() {
        repo.createCollection(userId, "MOVIES")
        repo.createCollection(userId, "GAMES")
        val collections = repo.getCollectionsForUser(userId)

        assertEquals(listOf("GAMES", "MOVIES"), collections.map { it.type })
    }

    @Test
    fun `setVisibility updates hidden field`() {
        repo.createCollection(userId, "GAMES")
        val result = repo.setVisibility(userId, "GAMES", true)

        assertTrue(result)

        val updated = repo.getCollectionsForUser(userId).first { it.type == "GAMES" }

        assertTrue(updated.hidden)
    }

    @Test
    fun `findCollectionId returns correct UUID`() {
        val collectionId = repo.createCollection(userId, "MOVIES")
        val foundId = repo.findCollectionId(userId, "movies")

        assertEquals(collectionId, foundId)
    }

    @Test
    fun `addItemToCollection and getItemsForCollection works`() {
        val collectionId = repo.createCollection(userId, "GAMES")
        val itemId = repo.addItemToCollection(collectionId, "item123", "Title", "Image", "Desc", "Source")
        val items = repo.getItemsForCollection(collectionId)

        assertEquals(1, items.size)

        val item = items.first()
        assertEquals("item123", item.itemId)
        assertEquals("Title", item.title)
        assertEquals("Image", item.imageUrl)
        assertEquals("Desc", item.description)
        assertEquals("Source", item.source)
    }

    @Test
    fun `removeItemFromCollection deletes item`() {
        val collectionId = repo.createCollection(userId, "GAMES")
        repo.addItemToCollection(collectionId, "item123")
        val removed = repo.removeItemFromCollection(collectionId, "item123")

        assertTrue(removed)
        assertTrue(repo.getItemsForCollection(collectionId).isEmpty())
    }

    @Test
    fun `createCollection returns UUID`() {
        val collectionId = repo.createCollection(userId, "BOOKS")
        val collections = repo.getCollectionsForUser(userId)

        assertTrue(collections.any { it.id == collectionId.toString() })
    }

    @Test
    fun `deleteCollection removes collection`() {
        val collectionId = repo.createCollection(userId, "MUSIC")
        val deleted = repo.deleteCollection(userId, collectionId.toString())

        assertTrue(deleted)

        val collections = repo.getCollectionsForUser(userId)

        assertFalse(collections.any { it.id == collectionId.toString() })
    }
}
