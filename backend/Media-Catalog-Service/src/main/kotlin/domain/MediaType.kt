package domain

import kotlinx.serialization.Serializable

@Serializable
enum class MediaType {
    GAMES, MOVIES, SHOWS, MUSIC, BOOKS, BOARDGAMES
}
