package controllers

import io.ktor.http.*
import io.ktor.server.routing.*
import io.ktor.server.response.*
import service.MusicService

class MusicController(
    private val musicService: MusicService
) : Controller {

    override fun register(routing: Routing) {
        routing.get("/api/v1/media/music") {
                val q = call.request.queryParameters["q"] ?: return@get call.respondText(
                    "Missing query parameter 'q'", status = HttpStatusCode.BadRequest
                )
                val limit = call.request.queryParameters["limit"]?.toIntOrNull() ?: 20
                val offset = call.request.queryParameters["offset"]?.toIntOrNull() ?: 0

                try {
                    val result = musicService.search(q, limit, offset)
                    call.respond(result)
                } catch (e: IllegalArgumentException) {
                    call.respond(HttpStatusCode.BadRequest, mapOf("error" to e.message))
                } catch (e: integration.spotify.SpotifyClientImpl.RateLimitException) {
                    call.respond(HttpStatusCode.TooManyRequests, mapOf("error" to "Spotify rate limit", "retryAfter" to e.retryAfterSeconds))
                } catch (e: Exception) {
                    call.application.environment.log.error("music search error", e)
                    call.respond(HttpStatusCode.InternalServerError, mapOf("error" to "internal error"))
                }
            }
        }
    }
