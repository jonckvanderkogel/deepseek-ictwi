package org.bullit.dsictwi.config

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "prompt")
data class PromptConfig(
    val system: SystemPromptConfig,
    val user: UserPromptConfig
) {
    data class SystemPromptConfig(
        val base: String,
        val rules: List<String>,
        val contextHeader: String
    )

    data class UserPromptConfig(
        val instructions: List<String>,
        val exampleFormat: String,
        val targetHeader: String
    )
}