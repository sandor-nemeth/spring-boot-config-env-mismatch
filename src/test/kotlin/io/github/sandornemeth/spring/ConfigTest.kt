package io.github.sandornemeth.spring

import org.assertj.core.api.Assertions
import org.assertj.core.api.SoftAssertions
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.AnnotationConfigApplicationContext
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.env.StandardEnvironment
import org.springframework.core.env.SystemEnvironmentPropertySource
import org.springframework.mock.env.MockEnvironment
import org.springframework.stereotype.Component

class ConfigTest {
    lateinit var ctx : AnnotationConfigApplicationContext

    @AfterEach
    fun teardown() {
        ctx.close()
    }

    @Test
    fun `resolve environment value with _ `() {
        val sysenv = SystemEnvironmentPropertySource(StandardEnvironment.SYSTEM_ENVIRONMENT_PROPERTY_SOURCE_NAME, mapOf("PROPS_CONF_VALUE" to "envValue"))

        val env = MockEnvironment().withProperty("props.confValue", "value")
        env.propertySources.addFirst(sysenv)

        ctx = AnnotationConfigApplicationContext()
        ctx.register(TestConfig::class.java, AutowiredValueBean::class.java)
        ctx.environment = env

        ctx.refresh()

        assertProperties()
    }

    @Test
    fun `resolve environment value without _`() {
        val sysenv = SystemEnvironmentPropertySource(StandardEnvironment.SYSTEM_ENVIRONMENT_PROPERTY_SOURCE_NAME, mapOf("PROPS_CONFVALUE" to "envValue"))

        val env = MockEnvironment().withProperty("props.confValue", "value")
        env.propertySources.addFirst(sysenv)

        ctx = AnnotationConfigApplicationContext()
        ctx.register(TestConfig::class.java, AutowiredValueBean::class.java)
        ctx.environment = env

        ctx.refresh()

        assertProperties()
    }

    fun assertProperties() {
        val valueFromConfigProperties = ctx.getBean(ConfigProps::class.java).confValue
        val valueFromValueAnnotation = ctx.getBean(AutowiredValueBean::class.java).confValue

        val softly = SoftAssertions()
        softly.assertThat(valueFromConfigProperties)
            .describedAs("Value from @ConfigurationProperties should equal to value from @Value annotation")
            .isEqualTo(valueFromValueAnnotation)
        softly.assertThat(valueFromConfigProperties)
            .describedAs("Value from configuration properties should be resolved from the environment variable")
            .isEqualTo("envValue")
        softly.assertThat(valueFromValueAnnotation)
            .describedAs("Value from @Value annotation should be resolved from the environment variable")
            .isEqualTo("envValue")
        softly.assertAll()
    }

    @ConfigurationProperties("props")
    class ConfigProps {
        var confValue: String? = null
    }

    @Component
    class AutowiredValueBean() {
        @Value("\${props.confValue}")
        lateinit var confValue: String
    }

    @Configuration
    @EnableConfigurationProperties(ConfigProps::class)
    class TestConfig {
    }
}