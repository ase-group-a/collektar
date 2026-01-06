package integration.bgg

import io.ktor.server.application.ApplicationEnvironment

data class BggConfig(
    val baseUrl: String,
    val token: String?,
    val minDelayMillis: Long
) {
    companion object {
        fun fromEnv(env: ApplicationEnvironment): BggConfig =
            BggConfig(
                baseUrl = env.config.propertyOrNull("BGG_BASE_URL")?.getString()
                    ?: "https://boardgamegeek.com/xmlapi2",
                token = env.config.propertyOrNull("BGG_API_TOKEN")?.getString(),
                minDelayMillis = env.config.propertyOrNull("BGG_MIN_DELAY_MS")?.getString()?.toLongOrNull() ?: 5000L
            )
    }
}