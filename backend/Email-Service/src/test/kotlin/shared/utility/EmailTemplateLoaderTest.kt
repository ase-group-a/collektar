package com.collektar.shared.utility

import com.collektar.config.TemplateLoaderConfig
import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNotEquals
import kotlin.test.assertTrue

class EmailTemplateLoaderTest {
    private lateinit var loader: IEmailTemplateLoader

    @BeforeEach
    fun setup() {
        val config = TemplateLoaderConfig(templateFolder = "templates")
        loader = EmailTemplateLoader(config)
    }

    @Test
    fun shouldLoadTemplateFromResource() {
        val expectedTemplate = """<!DOCTYPE html>
<html lang="en">
    <body>
        <h1>Test Template</h1>
        <p>This is a test email template.</p>
    </body>
</html>"""

        val template = loader.loadTemplate("test-template.html")

        assertTrue(template.isNotEmpty())
        assertEquals(template, expectedTemplate)
    }

    @Test
    fun shouldReturnConsistentContent() {
        val expectedTemplate = """<!DOCTYPE html>
<html lang="en">
    <body>
        <h1>Test Template</h1>
        <p>This is a test email template.</p>
    </body>
</html>"""

        val content1 = loader.loadTemplate("test-template.html")
        val content2 = loader.loadTemplate("test-template.html")

        assertEquals(content1, content2)
        assertEquals(content1, expectedTemplate)
        assertEquals(content2, expectedTemplate)
    }

    @Test
    fun shouldLoadMultipleDifferentTemplates() {
        val expectedTemplate1 = "Template1"
        val expectedTemplate2 = "Template2"

        val template1 = loader.loadTemplate("template1.html")
        val template2 = loader.loadTemplate("template2.html")

        assertNotEquals(template1, template2)
        assertEquals(template1, expectedTemplate1)
        assertEquals(template2, expectedTemplate2)
    }

    @Test
    fun shouldThrowWhenTemplateNotFound() {
        assertFailsWith<IllegalStateException> {
            loader.loadTemplate("does-not-exist.html")
        }
    }

    @Test
    fun shouldThrowWhenClassLoaderReturnsNull() {
        val mockClassLoader = mockk<ClassLoader>()
        every { mockClassLoader.getResourceAsStream(any()) } returns null

        val config = TemplateLoaderConfig(templateFolder = "templates")
        val loader = EmailTemplateLoader(config, mockClassLoader)

        assertFailsWith<IllegalStateException> { loader.loadTemplate(("template1.html")) }
    }
}