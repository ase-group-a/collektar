package controllers

import io.ktor.server.routing.*
import service.BggMediaService

class BoardGameController(
    private val bggMediaService: BggMediaService
) : Controller {

    override fun register(routing: Routing) {
        routing.mediaRoute("boardgames") {
            get {
                val q = call.queryParam("q")?.trim()
                val limit = call.queryParamInt("limit", 20)
                val offset = call.queryParamInt("offset", 0)

                call.safeCall {
                    if (q.isNullOrBlank()) {
                        bggMediaService.hot(limit, offset)   // ✅ fallback for empty q
                    } else {
                        bggMediaService.search(q, limit, offset)
                    }
                }
            }
        }
    }
}
