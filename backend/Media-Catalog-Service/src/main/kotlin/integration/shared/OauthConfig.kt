package com.collektar.integration.shared

import com.collektar.di.modules.OauthParameterType

interface OauthConfig {
    val clientId: String
    val clientSecret: String
    val baseUrl: String
    val tokenUrl: String
    val oauthParameterType: OauthParameterType
}