package org.bullit.dsictwi.config

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "deepseek")
data class DeepSeekConfig(
    val apiKey: String,
    val model: String,
    val temperature: Double,
    val baseUrl: String,
    val chatUrl: String
)