package com.collektar.imagecache

import com.collektar.config.ConfigUtils.getConfigValue
import com.collektar.config.ConfigUtils.getConfigValueInt
import com.collektar.config.ConfigUtils.getConfigValueLong
import io.ktor.server.application.ApplicationEnvironment

data class ImageCacheConfig (
    val urlPrefix: String,
    val redisUrl: String,
    val redisPoolSize: Int,
    val cacheTTL: Long,
    val spotifyUrlPrefix: String,
    val tmdbUrlPrefix: String,
    val igdbUrlPrefix: String,
    val googleBooksUrlPrefix: String,
    val bggUrlPrefix: String
) {
    companion object {
        fun fromEnv(env: ApplicationEnvironment): ImageCacheConfig {
            return ImageCacheConfig(
                urlPrefix = getConfigValue(env, "IMAGE_CACHE_URL_PREFIX", "imageCache.urlPrefix"),
                redisUrl = getConfigValue(env, "IMAGE_CACHE_REDIS_URL", "imageCache.redisUrl"),
                redisPoolSize = getConfigValueInt(env, "IMAGE_CACHE_REDIS_POOL_SIZE", "imageCache.redisPoolSize", 10),
                cacheTTL = getConfigValueLong(env, "IMAGE_CACHE_TTL", "imageCache.cacheTTL", 60),
                spotifyUrlPrefix = getConfigValue(env, "SPOTIFY_IMAGE_URL", "imageCache.spotifyUrlPrefix"),
                tmdbUrlPrefix = getConfigValue(env, "TMDB_IMAGE_URL", "imageCache.tmdbUrlPrefix"),
                igdbUrlPrefix = getConfigValue(env, "IGDB_IMAGE_URL", "imageCache.igdbUrlPrefix"),
                googleBooksUrlPrefix = getConfigValue(env, "GOOGLE_BOOKS_IMAGE_URL", "imageCache.googleBooksUrlPrefix"),
                bggUrlPrefix = getConfigValue(env, "BGG_IMAGE_URL", "imageCache.bggUrlPrefix", "https://cf.geekdo-images.com/")
            )
        }
    }
}