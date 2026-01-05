package com.collektar.config

abstract class BaseConfig {
    companion object {
        fun env(name: String, default: String? = null): String {
            return System.getenv(name) ?: default ?: error("Missing environment variable: $name")
        }
    }
}