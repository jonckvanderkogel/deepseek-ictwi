package org.bullit.dsictwi.config

import arrow.core.getOrElse
import org.bullit.dsictwi.prompts.CodeReader
import org.bullit.dsictwi.similarity.SimilarityService
import org.bullit.dsictwi.similarity.buildCorpus
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

    @Bean
    fun similarityService(
        codeReader: CodeReader,
        appConfig: AppConfig
    ): SimilarityService {
        val corpus = codeReader
            .getCodePairs()
            .map { codePairs ->
                buildCorpus(
                    codePairs.map { it.plsql },
                    appConfig.ngramSize
                )
            }
            .getOrElse { throw RuntimeException("Could not build corpus") }

        return SimilarityService(corpus, appConfig.ngramSize)
    }
}