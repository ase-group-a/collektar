package com.collektar.imagecache

interface ImageCacheClient {
    /**
     * Returns a JPEG image for sending to the client, either from the cache, or from the source
     * @param imageSource The source of the image
     * @param imageId The id of the image
     * @return The JPEG image as a Byte array
     */
    suspend fun getImage(imageSource: ImageSource, imageId: String): ByteArray

    /**
     * Prefetches images for storage in the cache, allowing for clients to load images faster
     * @param imageSource The source of the image
     * @param imageIdentifier The id or url of the image, depending on the source of the image
     * @return The URL generated for the image, that can be sent to the client
     */
    suspend fun prefetchImage(imageSource: ImageSource, imageIdentifier: String): String
}