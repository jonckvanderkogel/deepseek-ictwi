package org.bullit.dsictwi

import org.bullit.dsictwi.config.DeepSeekConfig
import org.bullit.dsictwi.config.PromptConfig
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.boot.runApplication

@EnableConfigurationProperties(DeepSeekConfig::class, PromptConfig::class)
@SpringBootApplication
class DeepSeekIctwiApplication

fun main(args: Array<String>) {
	runApplication<DeepSeekIctwiApplication>(*args)
}
