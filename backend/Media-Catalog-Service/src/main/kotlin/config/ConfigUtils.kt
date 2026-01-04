package com.collektar.config

import io.ktor.server.application.ApplicationEnvironment

object ConfigUtils {

    fun getConfigValue(
        env: ApplicationEnvironment,
        keyEnv: String,
        keyConf: String,
        default: String? = null
    ): String =
        env.config.propertyOrNull(keyConf)?.getString()
            ?: System.getenv(keyEnv)
            ?: default
            ?: error("$keyEnv not set")

    fun getConfigValueInt(
        env: ApplicationEnvironment,
        keyEnv: String,
        keyConf: String,
        default: Int? = null
    ): Int = env.config.propertyOrNull(keyConf)?.getString()?.toIntOrNull()
        ?: System.getenv(keyEnv)?.toIntOrNull()
        ?: default
        ?: error("$keyEnv not set")

    fun getConfigValueLong(
        env: ApplicationEnvironment,
        keyEnv: String,
        keyConf: String,
        default: Long? = null
    ): Long = env.config.propertyOrNull(keyConf)?.getString()?.toLongOrNull()
        ?: System.getenv(keyEnv)?.toLongOrNull()
        ?: default
        ?: error("$keyEnv not set")
}
