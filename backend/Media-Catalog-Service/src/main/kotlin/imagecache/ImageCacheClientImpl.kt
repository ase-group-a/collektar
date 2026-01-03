package com.collektar.imagecache

import io.ktor.client.HttpClient
import io.ktor.client.request.get
import io.ktor.client.statement.bodyAsBytes
import io.ktor.client.statement.bodyAsText
import io.ktor.http.isSuccess
import redis.clients.jedis.ConnectionPoolConfig
import redis.clients.jedis.RedisClient
import java.time.Duration

class ImageCacheClientImpl(
    private val httpClient: HttpClient,
    private val config: ImageCacheConfig
) : ImageCacheClient {
    
    private var redis : RedisClient? = null

    init {
        // Create redis pool instead of opening a new connection for every request
        val poolConfig = ConnectionPoolConfig().apply {
            maxTotal = config.redisPoolSize
            maxIdle = config.redisPoolSize
            minIdle = 0
            blockWhenExhausted = true
            setMaxWait(Duration.ofSeconds(1))
            testWhileIdle = true
            timeBetweenEvictionRuns = Duration.ofSeconds(1)
        }
        
        redis = RedisClient.builder()
            .fromURI(config.redisUrl)
            .poolConfig(poolConfig)
            .build()
        
        // Add closing of the pool on shutdown
        Runtime.getRuntime().addShutdownHook(Thread {
            redis?.close()
        })
    }
    
    override suspend fun getImage(
        imageSource: ImageSource,
        imageId: String
    ): ByteArray {
        if (redis == null) {
            throw IllegalStateException("Redis connection is unavailable, unable to serve images.")
        }
        
        val imageKey = "$imageSource:$imageId".toByteArray()
        
        // Get image from Redis cache
        var image = redis!!.get(imageKey)
        if (image == null) {
            // Image does not exist in the cache, retrieve it from the external service
            image = getImageFromService(imageSource, imageId)
            redis!!.set(imageKey, image)
        }
        
        return image
    }

    /**
     * Retrieves the image from the external service
     */
    private suspend fun getImageFromService (imageSource: ImageSource, imageId: String) : ByteArray {
        val imageUrl = when (imageSource) {
            ImageSource.SPOTIFY -> "${config.spotifyUrlPrefix}$imageId"
            ImageSource.TMBD -> "${config.tmdbUrlPrefix}$imageId.jpg"
            ImageSource.IGDB -> "${config.igdbUrlPrefix}$imageId.jpg"
            ImageSource.GOOGLE_BOOKS -> "${config.googleBooksUrlPrefix}$imageId"
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