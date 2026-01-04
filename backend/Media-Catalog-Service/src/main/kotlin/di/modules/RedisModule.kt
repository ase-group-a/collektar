package com.collektar.di.modules

import com.collektar.imagecache.ImageCacheConfig
import org.koin.dsl.module
import redis.clients.jedis.ConnectionPoolConfig
import redis.clients.jedis.RedisClient
import java.time.Duration

fun redisModule() = module {
    single<RedisClient>{
        val config : ImageCacheConfig = get()

        val poolConfig = ConnectionPoolConfig().apply {
            maxTotal = config.redisPoolSize
            maxIdle = config.redisPoolSize
            minIdle = 0
            blockWhenExhausted = true
            setMaxWait(Duration.ofSeconds(1))
            testWhileIdle = true
            timeBetweenEvictionRuns = Duration.ofSeconds(1)
        }

        val redis = RedisClient.builder()
            .fromURI(config.redisUrl)
            .poolConfig(poolConfig)
            .build()

        // Add closing of the pool on shutdown
        Runtime.getRuntime().addShutdownHook(Thread {
            redis?.close()
        })
        
        redis
    }
}