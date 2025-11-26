package com.collektar.integration.igdb

import com.collektar.config.ConfigUtils.getConfigValue
import com.collektar.di.modules.OauthParameterType
import com.collektar.integration.shared.OauthConfig
import io.ktor.server.application.ApplicationEnvironment

data class IGDBConfig (
    override val clientId: String,
    override val clientSecret: String,
    override val baseUrl: String,
    override val tokenUrl: String,
    override val oauthParameterType: OauthParameterType = OauthParameterType.URL_PARAMETER_URLENCODED
) : OauthConfig {
    companion object {
        fun fromEnv(env: ApplicationEnvironment): IGDBConfig {
            return IGDBConfig(
                clientId = getConfigValue(env, "IGDB_CLIENT_ID", "igdb.clientId"),
                clientSecret = getConfigValue(env, "IGDB_CLIENT_SECRET", "igdb.clientSecret"),
                baseUrl = getConfigValue(env, "IGDB_BASE_URL", "igdb.baseUrl"),
                tokenUrl = getConfigValue(env, "IGDB_TOKEN_URL", "igdb.tokenUrl"),
            )
        }
    }
}