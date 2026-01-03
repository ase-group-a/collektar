package com.collektar.controllers

import com.collektar.service.ImageCacheService
import controllers.Controller
import controllers.mediaRoute
import controllers.queryParam
import controllers.safeCall
import io.ktor.server.routing.Routing
import io.ktor.server.routing.get

class ImageCacheController (
    private val imageCacheService: ImageCacheService
) : Controller {
    override fun register(routing: Routing) {
        routing.mediaRoute("images") {
            get { 
                val imageSource = call.queryParam("source")
                val imageId = call.queryParam("id")
                
                call.safeCall { 
                    imageCacheService.getImage(imageSource, imageId)
                }
            }
        }
    }
}