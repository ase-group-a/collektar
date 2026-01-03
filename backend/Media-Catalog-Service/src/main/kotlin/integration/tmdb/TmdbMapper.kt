package integration.tmdb


import domain.MediaItem
import domain.MediaType

object TmdbMapper {
        fun movieToMediaItem(movie: TmdbMovieDto, imageIdentifierMapper: (String) -> String): MediaItem {
            return MediaItem(
                id = "tmdb:movie:${movie.id}",
                title = movie.title,
                type = MediaType.MOVIE,
                imageUrl = if (movie.posterPath != null) imageIdentifierMapper(movie.posterPath) else null,
                description = movie.overview,
                source = "tmdb"
            )
        }
}