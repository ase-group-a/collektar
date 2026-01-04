package com.collektar.di.modules

import com.collektar.controllers.ImageCacheController
import com.collektar.imagecache.ImageCacheClient
import com.collektar.imagecache.ImageCacheClientImpl
import com.collektar.imagecache.ImageCacheConfig
import com.collektar.service.ImageCacheService
import controllers.Controller
import io.ktor.server.application.ApplicationEnvironment
import org.koin.core.qualifier.named
import org.koin.dsl.module

const val IMAGE_CACHE_CONTROLLER_NAME = "images"

fun imageCacheModule(env: ApplicationEnvironment) = module {
    single { ImageCacheConfig.fromEnv(env) }
    single<ImageCacheClient> { ImageCacheClientImpl(get(), get(), get()) }
    single<ImageCacheService> { ImageCacheService(get()) }
    single<Controller>(named(IMAGE_CACHE_CONTROLLER_NAME)) { ImageCacheController(get()) }
}