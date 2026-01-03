package com.collektar.imagecache

import io.ktor.http.*

object ImageUrlParser {
    fun extractImageId(imageSource: ImageSource, imageIdentifier: String): String {
        return when (imageSource) {
            ImageSource.SPOTIFY -> extractSpotifyImageId(imageIdentifier)
            ImageSource.TMBD -> extractTMDBImageId(imageIdentifier)
            ImageSource.GOOGLE_BOOKS -> extractGoogleBooksImageId(imageIdentifier)
            ImageSource.IGDB -> imageIdentifier // IGDB image identifiers need no further data extraction, as the
                                                // original value from the API only contains the image id.
        }
    }
    
    fun extractSpotifyImageId(url: String): String {
        val parsedUrl = Url(url)

        // Extract the last path segment of the URL, which is the id
        // Example URL: "https://i.scdn.co/image/IMG_ID_HERE"
        val id = parsedUrl.segments.last()
        return id
    }

    fun extractTMDBImageId(url: String): String {
        // Remove prefix '/' character and image format postfix
        // Example URL: "/IMG_ID_HERE.jpg"
        val filename = url.drop(1)
        val id = filename.split(".").first()
        return id
    }

    fun extractGoogleBooksImageId(url: String): String {
        val parsedUrl = Url(url)

        // Get id from query parameters
        // Example URL: "http://books.google.com/books/content?id=IMG_ID_HERE&printsec=frontcover&img=1&zoom=1&edge=curl&source=gbs_api"
        val id = parsedUrl.parameters["id"]

        if (id != null) {
            return id
        } else {
            throw IllegalArgumentException("Provided Google Books Image URL does not match the specified format.")
        }
    }
}