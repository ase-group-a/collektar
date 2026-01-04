package integration.tmdb

import domain.MediaItem
import domain.MediaType

object TmdbMapper {
    private const val IMAGE_BASE_URL = "https://image.tmdb.org/t/p/w500"

    fun movieToMediaItem(movie: TmdbMovieDto): MediaItem =
        MediaItem(
            id = "tmdb:movie:${movie.id}",
            title = movie.title,
            type = MediaType.MOVIE,
            imageUrl = movie.posterPath?.let { "$IMAGE_BASE_URL$it" },
            description = movie.overview,
            source = "tmdb"
        )

    fun showToMediaItem(show: TmdbShowDto): MediaItem =
        MediaItem(
            id = "tmdb:show:${show.id}",
            title = show.name,
            type = MediaType.SHOW,
            imageUrl = show.posterPath?.let { "$IMAGE_BASE_URL$it" },
            description = show.overview,
            source = "tmdb"
        )
}
