package domain

import kotlinx.serialization.Serializable

@Serializable
enum class MediaType {
    GAME, MOVIE, SHOW, MUSIC, BOOK
}
