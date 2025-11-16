package controllers

import io.ktor.server.routing.*
import service.MusicService

class MusicController(
    private val musicService: MusicService
) : Controller {

    override fun register(routing: Routing) {
        routing.mediaRoute("music") {
            get {
                val q = call.queryParam("q")
                val limit = call.queryParamInt("limit", 20)
                val offset = call.queryParamInt("offset", 0)

                call.safeCall {
                    musicService.search(q, limit, offset)
                }
            }
        }
    }
}
