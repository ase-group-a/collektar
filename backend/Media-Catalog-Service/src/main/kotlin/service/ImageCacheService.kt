package com.collektar.service

import com.collektar.imagecache.ImageCacheClient

class ImageCacheService (
    private val imageCacheClient: ImageCacheClient
) {
    suspend fun getImage(imageSource: String?, id: String?): ByteArray {
        if (imageSource == null || id == null) {
            throw IllegalArgumentException("Image source or id cannot be null.")
        }
        
        TODO("Not implemented yet")
    }
}