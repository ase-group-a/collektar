package com.collektar.config

data class TemplateLoaderConfig(
    val templateFolder: String,
) : BaseConfig() {
    companion object {
        fun fromEnv() = TemplateLoaderConfig(
            templateFolder = env("EMAIL_SERVICE_TEMPLATE_FOLDER", default = "templates")
        )
    }
}
