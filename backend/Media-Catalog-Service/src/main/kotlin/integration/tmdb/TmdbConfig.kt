package integration.tmdb

import io.ktor.server.application.*

data class TmdbConfig(
    val bearerToken: String,
    val baseUrl: String
) {
    companion object {
        fun fromEnv(env: ApplicationEnvironment): TmdbConfig {
            val bearer = System.getenv("TMDB_BEARER_TOKEN")
                ?: env.config.propertyOrNull("tmdb.bearerToken")?.getString()
                ?: error("TMDB_BEARER_TOKEN not set")

            val base = System.getenv("TMDB_BASE_URL")
                ?: env.config.propertyOrNull("tmdb.baseUrl")?.getString()
                ?: "TMDB_BASE_URL not set"

            return TmdbConfig(bearer, base)
        }
    }
}
