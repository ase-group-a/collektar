package com.collektar.imagecache

import io.ktor.http.*

object ImageUrlParser {
    fun extractImageId(imageSource: ImageSource, imageIdentifier: String): String {
        return when (imageSource) {
            ImageSource.SPOTIFY -> extractSpotifyImageId(imageIdentifier)
            ImageSource.TMBD -> extractTMDBImageId(imageIdentifier)
            ImageSource.GOOGLE_BOOKS -> extractGoogleBooksImageId(imageIdentifier)
            ImageSource.IGDB -> imageIdentifier
            ImageSource.BGG -> imageIdentifier
        }
    }

    fun extractSpotifyImageId(url: String): String {
        val parsedUrl = Url(url)
        val id = parsedUrl.segments.last()
        return id
    }

    fun extractTMDBImageId(url: String): String {
        val filename = url.drop(1)
        val id = filename.split(".").first()
        return id
    }

    fun extractGoogleBooksImageId(url: String): String {
        val parsedUrl = Url(url)
        val id = parsedUrl.parameters["id"]

        if (id != null) {
            return id
        } else {
            throw IllegalArgumentException("Provided Google Books Image URL does not match the specified format.")
        }
    }


}