package integration.bgg

import io.ktor.server.application.ApplicationEnvironment

data class BggConfig(
    val baseUrl: String,
    val token: String?,
    val minDelayMillis: Long
) {
    companion object {
        fun fromEnv(env: ApplicationEnvironment): BggConfig {
            return BggConfig(
                baseUrl = env.config.propertyOrNull("BGG_BASE_URL")?.getString()
                    ?: System.getenv("BGG_BASE_URL")
                    ?: "https://boardgamegeek.com/xmlapi2",
                token = env.config.propertyOrNull("BGG_API_TOKEN")?.getString()
                    ?: System.getenv("BGG_API_TOKEN"),
                minDelayMillis = (env.config.propertyOrNull("BGG_MIN_DELAY_MS")?.getString()
                    ?: System.getenv("BGG_MIN_DELAY_MS"))?.toLongOrNull() ?: 5000L
            )
        }
    }
}