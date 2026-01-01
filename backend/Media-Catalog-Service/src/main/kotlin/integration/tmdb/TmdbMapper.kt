package integration.tmdb


import domain.MediaItem
import domain.MediaType

object TmdbMapper {
        private const val IMAGE_BASE_URL = "https://image.tmdb.org/t/p/w500"

        fun movieToMediaItem(movie: TmdbMovieDto): MediaItem {
            return MediaItem(
                id = "tmdb:movie:${movie.id}",
                title = movie.title,
                type = MediaType.MOVIES,
                imageUrl = movie.posterPath?.let { "$IMAGE_BASE_URL$it" },
                description = movie.overview,
                source = "tmdb"
            )
        }
}