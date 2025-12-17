package com.collektar.features.collection.repository

import com.collektar.dto.CollectionInfo
import com.collektar.dto.CollectionItemInfo
import com.collektar.features.collection.model.CollectionType
import com.collektar.shared.database.Tables
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.transactions.transaction
import java.util.*

class CollectionRepository {

    fun ensureDefaultCollections(userId: UUID) {
        transaction {
            val existingTypes: Set<CollectionType> =
                Tables.Collections
                    .selectAll()
                    .where { Tables.Collections.userId eq userId }
                    .map { it[Tables.Collections.type] }
                    .toSet()

            CollectionType.entries
                .filterNot { it in existingTypes }
                .forEach { type ->
                    Tables.Collections.insert {
                        it[id] = UUID.randomUUID()
                        it[Tables.Collections.userId] = userId
                        it[Tables.Collections.type] = type
                        it[Tables.Collections.hidden] = false
                        it[Tables.Collections.createdAt] = System.currentTimeMillis()
                    }
                }
        }
    }

    fun getCollectionsForUser(userId: UUID): List<CollectionInfo> = transaction {
        Tables.Collections
            .selectAll()
            .where { Tables.Collections.userId eq userId }
            .orderBy(Tables.Collections.type to SortOrder.ASC)
            .map {
                CollectionInfo(
                    id = it[Tables.Collections.id].toString(),
                    type = it[Tables.Collections.type].name,
                    hidden = it[Tables.Collections.hidden],
                    createdAt = it[Tables.Collections.createdAt]
                )
            }
    }

    fun setVisibility(userId: UUID, type: CollectionType, hidden: Boolean): Boolean = transaction {
        Tables.Collections.update({
            (Tables.Collections.userId eq userId) and
                    (Tables.Collections.type eq type)
        }) {
            it[Tables.Collections.hidden] = hidden
        } > 0
    }

    fun findCollectionId(userId: UUID, type: CollectionType): UUID? = transaction {
        Tables.Collections
            .selectAll()
            .where {
                (Tables.Collections.userId eq userId) and
                        (Tables.Collections.type eq type)
            }
            .map { it[Tables.Collections.id] }
            .singleOrNull()
    }

    fun addItemToCollection(
        collectionId: UUID,
        itemId: String,
        title: String?,
        imageUrl: String?,
        description: String?,
        source: String?
    ): UUID = transaction {
        val newId = UUID.randomUUID()
        Tables.CollectionItems.insert { stmt ->
            stmt[Tables.CollectionItems.id] = newId
            stmt[Tables.CollectionItems.collectionId] = collectionId
            stmt[Tables.CollectionItems.itemId] = itemId
            stmt[Tables.CollectionItems.title] = title
            stmt[Tables.CollectionItems.imageUrl] = imageUrl
            stmt[Tables.CollectionItems.description] = description
            stmt[Tables.CollectionItems.itemSource] = source
            stmt[Tables.CollectionItems.createdAt] = System.currentTimeMillis()
        }
        newId
    }

    fun getItemsForCollection(collectionId: UUID): List<CollectionItemInfo> = transaction {
        Tables.CollectionItems
            .selectAll()
            .where { Tables.CollectionItems.collectionId eq collectionId }
            .orderBy(Tables.CollectionItems.createdAt to SortOrder.ASC)
            .map { row ->
                CollectionItemInfo(
                    id = row[Tables.CollectionItems.id].toString(),
                    itemId = row[Tables.CollectionItems.itemId],
                    title = row[Tables.CollectionItems.title],
                    imageUrl = row[Tables.CollectionItems.imageUrl],
                    description = row[Tables.CollectionItems.description],
                    source = row[Tables.CollectionItems.itemSource],
                    addedAt = row[Tables.CollectionItems.createdAt]
                )
            }
    }

    fun removeItemFromCollection(collectionId: UUID, itemId: String): Boolean = transaction {
        Tables.CollectionItems.deleteWhere {
            (Tables.CollectionItems.collectionId eq collectionId) and
                    (Tables.CollectionItems.itemId eq itemId)
        } > 0
    }
}
