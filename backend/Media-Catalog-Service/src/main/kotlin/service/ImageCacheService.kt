package com.collektar.service

import com.collektar.imagecache.ImageCacheClient
import com.collektar.imagecache.ImageSource

class ImageCacheService (
    private val imageCacheClient: ImageCacheClient
) {
    suspend fun getImage(imageSource: String?, id: String?): ByteArray {
        if (imageSource == null || id == null) {
            throw IllegalArgumentException("Image source or id cannot be null.")
        }
        
        val imageSourceEnum = ImageSource.valueOf(imageSource.uppercase())
        return imageCacheClient.getImage(imageSourceEnum, id)
    }
}