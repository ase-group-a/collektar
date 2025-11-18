package integration.tmdb

import io.ktor.server.application.*

data class TmdbConfig(
    val bearerToken: String,
    val baseUrl: String
) {
    companion object {
        fun fromEnv(env: ApplicationEnvironment): TmdbConfig {

            fun get(keyEnv: String, keyConf: String, default: String? = null): String =
                env.config.propertyOrNull(keyConf)?.getString()
                    ?: System.getenv(keyEnv)
                    ?: default
                    ?: error("$keyEnv not set")

            return TmdbConfig(
                bearerToken = get("TMDB_BEARER_TOKEN", "tmdb.bearerToken"),
                baseUrl = get("TMDB_BASE_URL", "tmdb.baseUrl", "https://api.themoviedb.org/3")
            )
        }
    }
}