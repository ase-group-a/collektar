package com.collektar.imagecache

import com.collektar.config.ConfigUtils.getConfigValue
import io.ktor.server.application.ApplicationEnvironment

data class ImageCacheConfig (
    // The url Prefix is used to generate the URL that is sent to the client within the MediaItem
    val urlPrefix: String,
    val redisUrl: String,
    // Below are prefixes for image URLs
    val spotifyUrlPrefix: String,
    val tmdbUrlPrefix: String,
    val igdbUrlPrefix: String,
    val googleBooksUrlPrefix: String,
) {
    companion object {
        fun fromEnv(env: ApplicationEnvironment): ImageCacheConfig {
            return ImageCacheConfig(
                urlPrefix = getConfigValue(env, "IMAGE_CACHE_URL_PREFIX", "imageCache.urlPrefix"),
                redisUrl = getConfigValue(env, "IMAGE_CACHE_REDIS_URL", "imageCache.redisUrl"),
                
                spotifyUrlPrefix = getConfigValue(env, "SPOTIFY_IMAGE_URL", "imageCache.spotifyUrlPrefix"),
                tmdbUrlPrefix = getConfigValue(env, "TMDB_IMAGE_URL", "imageCache.tmdbUrlPrefix"),
                igdbUrlPrefix = getConfigValue(env, "IGDB_IMAGE_URL", "imageCache.igdbUrlPrefix"),
                googleBooksUrlPrefix = getConfigValue(env, "GOOGLE_BOOKS_IMAGE_URL", "imageCache.googleBooksUrlPrefix")
            )
        }
    }
}