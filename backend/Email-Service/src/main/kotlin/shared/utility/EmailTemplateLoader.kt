package com.collektar.shared.utility

import com.collektar.config.TemplateLoaderConfig

class EmailTemplateLoader(
    private val config: TemplateLoaderConfig
): IEmailTemplateLoader {
    private var templateCache = mutableMapOf<String, String>()

    override fun loadTemplate(templateName: String): String {
        return templateCache.getOrPut(templateName) {
            loadTemplateContent(templateName)
        }
    }

    private fun loadTemplateContent(templateName: String): String {
        val resourcePath = "${config.templateFolder}/$templateName"

        return this::class.java.classLoader
            ?.getResourceAsStream(resourcePath)
            ?.bufferedReader()
            ?.use { it.readText() }
            ?: throw IllegalStateException(
                "Template '$templateName' not found at path: $resourcePath"
            )
    }
}