package integration.bgg

import io.ktor.server.application.*
import com.collektar.config.ConfigUtils.getConfigValue

data class BggConfig(
    val baseUrl: String,
    val token: String?,
    val minDelayMillis: Long
) {
    companion object {
        fun fromEnv(env: ApplicationEnvironment): BggConfig {
            return BggConfig(
                baseUrl = getConfigValue(
                    env,
                    "BGG_BASE_URL",
                    "bgg.baseUrl",
                    "https://boardgamegeek.com/xmlapi2"
                ),
                token = getConfigValue(
                    env,
                    "BGG_API_TOKEN",
                    "bgg.apiToken",
                ),
                minDelayMillis = getConfigValue(
                    env,
                    "BGG_MIN_DELAY_MS",
                    "bgg.minDelayMs",
                ).toLong()
            )
        }
    }
}
