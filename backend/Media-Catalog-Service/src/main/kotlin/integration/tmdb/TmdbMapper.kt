package integration.tmdb

import domain.MediaItem
import domain.MediaType

object TmdbMapper {
        fun movieToMediaItem(movie: TmdbMovieDto, imageIdentifierMapper: (String) -> String): MediaItem {
            return MediaItem(
                id = "tmdb:movie:${movie.id}",
                title = movie.title,
                type = MediaType.MOVIES,
                imageUrl = if (movie.posterPath != null) imageIdentifierMapper(movie.posterPath) else null,
                description = movie.overview,
                source = "tmdb"
            )
        }

    fun showToMediaItem(show: TmdbShowDto, imageIdentifierMapper: (String) -> String): MediaItem =
        MediaItem(
            id = "tmdb:show:${show.id}",
            title = show.name,
            type = MediaType.SHOWS,
            imageUrl = if (show.posterPath != null) imageIdentifierMapper(show.posterPath) else null,
            description = show.overview,
            source = "tmdb"
        )
}