package com.collektar.integration.igdb

import com.collektar.integration.shared.OauthConfig
import io.ktor.server.application.ApplicationEnvironment

data class IGDBConfig (
    override val clientId: String,
    override val clientSecret: String,
    override val baseUrl: String,
    override val tokenUrl: String
) : OauthConfig {
    companion object {
        fun fromEnv(env: ApplicationEnvironment): IGDBConfig {
            return IGDBConfig(
                clientId = env.config.property("IGDB_CLIENT_ID").getString(),
                clientSecret = env.config.property("IGDB_CLIENT_SECRET").getString(),
                baseUrl = env.config.property("IGDB_BASE_URL").getString(),
                tokenUrl = env.config.property("IGDB_TOKEN_URL").getString()
            )
        }
    }
}