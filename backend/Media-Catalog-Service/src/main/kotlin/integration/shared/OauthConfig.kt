package com.collektar.integration.shared

interface OauthConfig {
    val clientId: String
    val clientSecret: String
    val tokenUrl: String
}