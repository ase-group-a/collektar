package com.collektar.imagecache

import io.ktor.client.HttpClient

class ImageCacheClientImpl(
    private val httpClient: HttpClient,
    private val config: ImageCacheConfig
) : ImageCacheClient {
    override suspend fun getImage(
        imageSource: ImageSource,
        imageId: String
    ): ByteArray {
        TODO("Not yet implemented")
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