package com.collektar.shared.database

import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.transaction
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class TablesTest {

    companion object {
        @BeforeAll
        @JvmStatic
        fun setup() {
            Database.connect("jdbc:h2:mem:test;DB_CLOSE_DELAY=-1;", driver = "org.h2.Driver")

            transaction {
                SchemaUtils.create(Tables.Collections, Tables.CollectionItems)
            }
        }
    }

    @Test
    fun `Collections table has correct columns and primary key`() = transaction {
        val columns = Tables.Collections.columns.map { it.name }
        assertTrue("id" in columns)
        assertTrue("user_id" in columns)
        assertTrue("type" in columns)
        assertTrue("hidden" in columns)
        assertTrue("created_at" in columns)
        assertEquals("PK_Collections_Id", Tables.Collections.primaryKey?.name)
    }

    @Test
    fun `CollectionItems table has correct columns and primary key`() = transaction {
        val columns = Tables.CollectionItems.columns.map { it.name }
        assertTrue("id" in columns)
        assertTrue("collection_id" in columns)
        assertTrue("item_id" in columns)
        assertTrue("title" in columns)
        assertTrue("image_url" in columns)
        assertTrue("description" in columns)
        assertTrue("source" in columns)
        assertTrue("created_at" in columns)
        assertEquals("PK_CollectionItems_Id", Tables.CollectionItems.primaryKey?.name)
    }

    @Test
    fun `CollectionItems has foreign key reference to Collections`() = transaction {
        val refColumn = Tables.CollectionItems.collectionId
        val refTarget = refColumn.referee

        assertEquals(Tables.Collections.id, refTarget)

        val foreignKey = Tables.CollectionItems.indices.flatMap { it.columns }.any {
            it == Tables.CollectionItems.collectionId
        }
        assertTrue(foreignKey, "collectionId sollte eine Foreign Key Referenz auf Collections.id haben")
    }
}
