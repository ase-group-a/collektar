package com.collektar.features.collection.service

import com.collektar.dto.CollectionInfo
import com.collektar.dto.CollectionItemInfo
import com.collektar.features.collection.model.CollectionType
import com.collektar.features.collection.repository.CollectionRepository
import com.collektar.shared.database.Tables.CollectionItems.itemSource
import java.util.*

class CollectionService(
    private val repository: CollectionRepository = CollectionRepository()
) {

    fun ensureDefaults(userId: UUID) {
        repository.ensureDefaultCollections(userId)
    }

    fun listCollections(userId: UUID): List<CollectionInfo> {
        return repository.getCollectionsForUser(userId)
    }

    fun setVisibility(userId: UUID, type: CollectionType, hidden: Boolean) {
        val ok = repository.setVisibility(userId, type, hidden)
        if (!ok) throw NoSuchElementException("Collection not found for user")
    }

    fun addItem(userId: UUID, type: CollectionType, itemId: String, title: String?, imageUrl: String?, description: String?, source: String?): CollectionItemInfo {
        val collectionId = repository.findCollectionId(userId, type) ?: throw NoSuchElementException("Collection not found")
        val newId = repository.addItemToCollection(collectionId, itemId, title, imageUrl, description, source)
        return CollectionItemInfo(
            id = newId.toString(),
            itemId = itemId,
            title = title,
            imageUrl = imageUrl,
            description = description,
            source = source,
            addedAt = System.currentTimeMillis()
        )
    }

    fun listItems(userId: UUID, type: CollectionType): List<CollectionItemInfo> {
        val collectionId = repository.findCollectionId(userId, type) ?: throw NoSuchElementException("Collection not found")
        return repository.getItemsForCollection(collectionId)
    }


    fun removeItem(userId: UUID, type: CollectionType, itemId: String) {
        val collectionId = repository.findCollectionId(userId, type) ?: throw NoSuchElementException("Collection not found")
        val removed = repository.removeItemFromCollection(collectionId, itemId)
        if (!removed) throw NoSuchElementException("Item not found in collection")
    }
}
