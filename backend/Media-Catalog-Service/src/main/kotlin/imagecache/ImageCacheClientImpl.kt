package com.collektar.imagecache

import io.ktor.client.HttpClient
import io.ktor.client.request.get
import io.ktor.client.statement.bodyAsBytes
import io.ktor.client.statement.bodyAsText
import io.ktor.http.isSuccess
import redis.clients.jedis.RedisClient

class ImageCacheClientImpl(
    private val httpClient: HttpClient,
    private val config: ImageCacheConfig,
    private var redis: RedisClient
) : ImageCacheClient {
    override suspend fun getImage(
        imageSource: ImageSource,
        imageId: String
    ): ByteArray {
        val imageKey = "$imageSource:$imageId".toByteArray()

        // Get image from Redis cache
        var image = redis[imageKey]
        if (image == null) {
            image = getImageFromService(imageSource, imageId)
            redis.setex(imageKey, config.cacheTTL, image)
        }

        return image
    }

    private suspend fun getImageFromService(imageSource: ImageSource, imageId: String): ByteArray {
        val imageUrl = when (imageSource) {
            ImageSource.SPOTIFY -> "${config.spotifyUrlPrefix}$imageId"
            ImageSource.TMBD -> "${config.tmdbUrlPrefix}$imageId.jpg"
            ImageSource.IGDB -> "${config.igdbUrlPrefix}$imageId.jpg"
            ImageSource.GOOGLE_BOOKS -> "${config.googleBooksUrlPrefix}$imageId"
            ImageSource.BGG -> imageId
        }

        val response = httpClient.get(imageUrl)

        if (!response.status.isSuccess()) {
            val text = response.bodyAsText()
            throw RuntimeException("Failed to fetch image $imageId from service $imageSource: $text")
        }

        return response.bodyAsBytes()
    }

    override fun getImageUrl(
        imageSource: ImageSource,
        imageIdentifier: String
    ): String {
        return "${
            config.urlPrefix
        }?source=${
            imageSource.toString().lowercase()
        }&id=${
            ImageUrlParser.extractImageId(
                imageSource,
                imageIdentifier
            )
        }"
    }
}