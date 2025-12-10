package integration.bgg

data class BggConfig(
    val baseUrl: String,
    val token: String?,
    val minDelayMillis: Long
) {
    companion object {
        fun fromEnv(): BggConfig =
            BggConfig(
                baseUrl = System.getenv("BGG_BASE_URL")
                    ?: "https://boardgamegeek.com/xmlapi2",
                token = System.getenv("BGG_API_TOKEN"),
                // default 5 seconds between calls
                minDelayMillis = (System.getenv("BGG_MIN_DELAY_MS") ?: "5000").toLong()
            )
    }
}
