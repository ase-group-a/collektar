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
     * Returns an image url that can be that can be sent to the client and uniquely identifies the image and source for
     * retrieving it at a later point
     * @param imageSource The source of the image
     * @param imageIdentifier The id or url of the image, depending on the source of the image
     * @return The URL generated for the image
     */
    fun getImageUrl(imageSource: ImageSource, imageIdentifier: String): String
}