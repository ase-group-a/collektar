package com.collektar.features.collection.model

import kotlinx.serialization.Serializable

@Serializable
enum class CollectionType {
    GAMES, MOVIES, SHOWS, BOOKS, MUSIC, BOARDGAMES;

    companion object {
        fun fromString(s: String): CollectionType? = try {
            valueOf(s.uppercase())
        } catch (e: Exception) {
            null
        }
    }
}
