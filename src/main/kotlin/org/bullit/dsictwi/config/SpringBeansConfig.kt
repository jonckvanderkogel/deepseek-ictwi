package org.bullit.dsictwi.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.io.ResourceLoader
import org.springframework.core.io.support.PathMatchingResourcePatternResolver
import org.springframework.core.io.support.ResourcePatternResolver
import org.springframework.web.reactive.function.client.WebClient

@Configuration
class SpringBeansConfig {

    @Bean
    fun resourcePatternResolver(resourceLoader: ResourceLoader): ResourcePatternResolver =
        PathMatchingResourcePatternResolver(resourceLoader)

    @Bean
    fun webClient(config: DeepSeekConfig): WebClient =
        WebClient
            .builder()
            .baseUrl(config.baseUrl)
            .defaultHeader("Authorization", "Bearer ${config.apiKey}")
            .build()
}