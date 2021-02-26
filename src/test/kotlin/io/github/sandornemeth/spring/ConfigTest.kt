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

    /**
     * This seem to conflict https://docs.spring.io/spring-boot/docs/current/reference/html/spring-boot-features.html#boot-features-external-config
     */
    @Test
    fun reproduce() {
        val sysenv = SystemEnvironmentPropertySource(StandardEnvironment.SYSTEM_ENVIRONMENT_PROPERTY_SOURCE_NAME, mapOf("PROPS_CONF_VALUE" to "envValue"))

        val env = MockEnvironment().withProperty("props.confValue", "value")
        env.propertySources.addFirst(sysenv)

        ctx = AnnotationConfigApplicationContext()
        ctx.register(TestConfig::class.java, AutowiredValueBean::class.java)
        ctx.environment = env

        ctx.refresh()

        val softly = SoftAssertions()
        softly.assertThat(ctx.getBean(ConfigProps::class.java).confValue).isEqualTo("envValue")
        softly.assertThat(ctx.getBean(AutowiredValueBean::class.java).confValue).isEqualTo("envValue")
        softly.assertThat(ctx.getBean(ValueBean::class.java).confValue).isEqualTo("envValue")
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

    class ValueBean(val confValue: String)

    @Configuration
    @EnableConfigurationProperties(ConfigProps::class)
    class TestConfig {

        @Bean
        fun valueBean(@Value("\${props.confValue}") confValue: String) = ValueBean(confValue)
    }
}