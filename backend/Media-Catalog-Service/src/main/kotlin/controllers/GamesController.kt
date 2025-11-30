package controllers

import io.ktor.server.routing.Routing
import io.ktor.server.routing.get
import service.GamesService

class GamesController (
    private val gamesService: GamesService
) : Controller {
    override fun register(routing: Routing) {
        routing.mediaRoute("games") {
            get {
                val q = call.queryParam("q")
                val limit = call.queryParamInt("limit", 20)
                val offset = call.queryParamInt("offset", 0)
                
                call.safeCall { 
                    gamesService.search(q, limit, offset)
                }
            }
        }
    }
}