package controllers

import io.ktor.server.routing.*
import service.MovieService

class MovieController(
    private val movieService: MovieService
) : Controller {

    override fun register(routing: Routing) {
        routing.mediaRoute("movies") {
            get {
                val q = call.queryParam("q")
                val limit = call.queryParamInt("limit", 20)
                val offset = call.queryParamInt("offset", 0)
                call.safeCall { movieService.searchMovies(q, limit, offset ) }
            }
        }
    }
}



