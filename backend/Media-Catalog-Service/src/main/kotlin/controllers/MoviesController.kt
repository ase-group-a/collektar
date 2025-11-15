package controllers

import domain.SearchResult
import io.ktor.http.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import service.MovieService

class MovieController(
    private val movieService: MovieService
) : Controller {

    override fun register(routing: Routing) {
        routing.route("/movies") {

            get("/search/movies") {
                val query = call.request.queryParameters["q"]
                    ?: return@get call.respond(HttpStatusCode.BadRequest, "Missing query parameter 'q'")

                val limit = call.request.queryParameters["limit"]?.toIntOrNull() ?: 20
                val offset = call.request.queryParameters["offset"]?.toIntOrNull() ?: 0

                val result: SearchResult = movieService.searchMovies(query, limit, offset)
                call.respond(result)
            }
        }
    }
}