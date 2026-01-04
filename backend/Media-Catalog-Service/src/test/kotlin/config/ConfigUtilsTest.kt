package config

import com.collektar.config.ConfigUtils.getConfigValue
import com.collektar.config.ConfigUtils.getConfigValueInt
import com.collektar.config.ConfigUtils.getConfigValueLong
import io.ktor.server.application.ApplicationEnvironment
import io.ktor.server.config.MapApplicationConfig
import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.assertThrows
import org.junitpioneer.jupiter.SetEnvironmentVariable
import kotlin.test.Test
import kotlin.test.assertEquals

const val STRING_ENV_NAME = "ENV_NAME"
const val STRING_ENV = "string-env-var"
const val INT_ENV_NAME = "INT_ENV_NAME"
const val INT_ENV = "12345"
const val INT_VALUE = 12345
const val LONG_ENV_NAME = "LONG_ENV_NAME"
const val LONG_ENV = "${Int.MAX_VALUE.toLong() + 50}"
const val LONG_VALUE = Int.MAX_VALUE + 50L

class ConfigUtilsTest {

    @Test
    @SetEnvironmentVariable(key = STRING_ENV_NAME, value = STRING_ENV)
    fun `getConfigValue with environment variable`() {
        val env = mockk<ApplicationEnvironment>()
        every { env.config.propertyOrNull(STRING_ENV_NAME) } returns null
        
        val configValue = getConfigValue(env, STRING_ENV_NAME, STRING_ENV_NAME)
        assertEquals(STRING_ENV, configValue)
    }

    @Test
    fun `getConfigValue with ApplicationEnvironment config`() {
        val mapConfig = MapApplicationConfig(
            STRING_ENV_NAME to STRING_ENV,
        )
        
        val env = mockk<ApplicationEnvironment>()
        every { env.config } returns mapConfig
        
        val configValue = getConfigValue(env, STRING_ENV_NAME, STRING_ENV_NAME)
        assertEquals(STRING_ENV, configValue)
    }

    @Test
    fun `getConfigValue with default value`() {
        val env = mockk<ApplicationEnvironment>()
        every { env.config.propertyOrNull(STRING_ENV_NAME) } returns null

        val configValue = getConfigValue(env, STRING_ENV_NAME, STRING_ENV_NAME, STRING_ENV)
        assertEquals(STRING_ENV, configValue)
    }

    @Test
    fun `getConfigValue with all null values`() {
        val env = mockk<ApplicationEnvironment>()
        every { env.config.propertyOrNull(STRING_ENV_NAME) } returns null

        assertThrows<IllegalStateException>{
            getConfigValue(env, STRING_ENV_NAME, STRING_ENV_NAME)
        }
    }
    
    @Test
    @SetEnvironmentVariable(key = INT_ENV_NAME, value = INT_ENV)
    fun `getConfigValueInt with environment variable`() {
        val env = mockk<ApplicationEnvironment>()
        every { env.config.propertyOrNull(INT_ENV_NAME) } returns null

        val configValue = getConfigValueInt(env, INT_ENV_NAME, INT_ENV_NAME)
        assertEquals(INT_VALUE, configValue)
    }

    @Test
    fun `getConfigValueInt with ApplicationEnvironment config`() {
        val mapConfig = MapApplicationConfig(
            INT_ENV_NAME to INT_ENV,
        )

        val env = mockk<ApplicationEnvironment>()
        every { env.config } returns mapConfig

        val configValue = getConfigValueInt(env, INT_ENV_NAME, INT_ENV_NAME)
        assertEquals(INT_VALUE, configValue)
    }

    @Test
    fun `getConfigValueInt with default value`() {
        val env = mockk<ApplicationEnvironment>()
        every { env.config.propertyOrNull(INT_ENV_NAME) } returns null

        val configValue = getConfigValueInt(env, INT_ENV_NAME, INT_ENV_NAME, INT_VALUE)
        assertEquals(INT_VALUE, configValue)
    }

    @Test
    fun `getConfigValueInt with all null values`() {
        val env = mockk<ApplicationEnvironment>()
        every { env.config.propertyOrNull(INT_ENV_NAME) } returns null

        assertThrows<IllegalStateException>{
            getConfigValueInt(env, INT_ENV_NAME, INT_ENV_NAME)
        }
    }

    @Test
    @SetEnvironmentVariable(key = LONG_ENV_NAME, value = LONG_ENV)
    fun `getConfigValueLong with environment variable`() {
        val env = mockk<ApplicationEnvironment>()
        every { env.config.propertyOrNull(LONG_ENV_NAME) } returns null

        val configValue = getConfigValueLong(env, LONG_ENV_NAME, LONG_ENV_NAME)
        assertEquals(LONG_VALUE, configValue)
    }

    @Test
    fun `getConfigValueLong with ApplicationEnvironment config`() {
        val mapConfig = MapApplicationConfig(
            LONG_ENV_NAME to LONG_ENV,
        )

        val env = mockk<ApplicationEnvironment>()
        every { env.config } returns mapConfig

        val configValue = getConfigValueLong(env, LONG_ENV_NAME, LONG_ENV_NAME)
        assertEquals(LONG_VALUE, configValue)
    }

    @Test
    fun `getConfigValueLong with default value`() {
        val env = mockk<ApplicationEnvironment>()
        every { env.config.propertyOrNull(LONG_ENV_NAME) } returns null

        val configValue = getConfigValueLong(env, LONG_ENV_NAME, LONG_ENV_NAME, LONG_VALUE)
        assertEquals(LONG_VALUE, configValue)
    }

    @Test
    fun `getConfigValueLong with all null values`() {
        val env = mockk<ApplicationEnvironment>()
        every { env.config.propertyOrNull(LONG_ENV_NAME) } returns null

        assertThrows<IllegalStateException>{
            getConfigValueLong(env, LONG_ENV_NAME, LONG_ENV_NAME)
        }
    }
}