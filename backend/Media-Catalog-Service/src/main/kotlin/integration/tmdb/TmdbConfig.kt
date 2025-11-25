package integration.tmdb

import com.collektar.config.ConfigUtils.getConfigValue
import io.ktor.server.application.*

data class TmdbConfig(
    val bearerToken: String,
    val baseUrl: String
) {
    companion object {
        fun fromEnv(env: ApplicationEnvironment): TmdbConfig =
            TmdbConfig(
                bearerToken = getConfigValue(env, "TMDB_BEARER_TOKEN", "tmdb.bearerToken"),
                baseUrl = getConfigValue(
                    env,
                    "TMDB_BASE_URL",
                    "tmdb.baseUrl",
                    "https://api.themoviedb.org/3"
                )
            )
    }
}
