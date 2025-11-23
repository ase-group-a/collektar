package controllers

import io.ktor.server.routing.Routing
import service.MusicService
import service.MovieService

// TODO: fix manual registering later. Initial automatic registering led to problems when registering multiple APIs at the same time, this is a temporary fix

class ControllerRegistry(
    private val musicService: MusicService,
    private val movieService: MovieService
) {
    private val getControllers: List<Controller> by lazy {
        listOf(
            MusicController(musicService),
            MovieController(movieService)
        )
    }

    fun registerAll(routing: Routing) {
        getControllers.forEach { it.register(routing) }
    }
}
