package com.collektar.imagecache

import io.ktor.client.HttpClient
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