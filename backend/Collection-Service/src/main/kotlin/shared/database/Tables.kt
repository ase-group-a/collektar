package com.collektar.shared.database

import com.collektar.features.collection.model.CollectionType
import org.jetbrains.exposed.sql.ReferenceOption
import org.jetbrains.exposed.sql.Table

object Tables {

    object Collections : Table("collections") {
        val id = uuid("id").uniqueIndex()
        val userId = uuid("user_id").index()
        val type = enumerationByName("type", 50, CollectionType::class)
        val hidden = bool("hidden").default(false)
        val createdAt = long("created_at").clientDefault { System.currentTimeMillis() }

        override val primaryKey = PrimaryKey(id, name = "PK_Collections_Id")

        init {
            index(true, userId, type)
        }
    }

    object CollectionItems : Table("collection_items") {
        val id = uuid("id").uniqueIndex()
        val collectionId = reference("collection_id", Collections.id, onDelete = ReferenceOption.CASCADE)
        val itemId = varchar("item_id", length = 255)
        val createdAt = long("created_at").clientDefault { System.currentTimeMillis() }

        override val primaryKey = PrimaryKey(id, name = "PK_CollectionItems_Id")

        init {
            index(true, collectionId, itemId)
            index(false, collectionId)
        }
    }
}