package com.collektar.shared.utility

import com.collektar.config.TemplateLoaderConfig

class EmailTemplateLoader(
    private val config: TemplateLoaderConfig,
    private val classLoader: ClassLoader = EmailTemplateLoader::class.java.classLoader
        ?: throw IllegalStateException("ClassLoader not available")
): IEmailTemplateLoader {
    private var templateCache = mutableMapOf<String, String>()

    override fun loadTemplate(templateName: String): String {
        return templateCache.getOrPut(templateName) {
            loadTemplateContent(templateName)
        }
    }

    private fun loadTemplateContent(templateName: String): String {
        val resourcePath = "${config.templateFolder}/$templateName"

        val resourceStream = classLoader.getResourceAsStream(resourcePath)
            ?: throw IllegalStateException("Template '$templateName' not found at path: $resourcePath")

        return resourceStream.bufferedReader().use { it.readText() }
    }
}