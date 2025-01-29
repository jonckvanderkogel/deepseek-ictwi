package org.bullit.dsictwi.config

import io.github.resilience4j.core.IntervalFunction
import io.github.resilience4j.retry.RetryConfig
import io.github.resilience4j.retry.RetryRegistry
import org.bullit.dsictwi.logger
import org.bullit.dsictwi.prompts.DeepSeekClient.DeepSeekResponse
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.reactive.function.client.WebClientRequestException
import java.io.IOException

@Configuration
class Resilience4jConfig {
    companion object {
        const val DEEPSEEK_CLIENT = "deepSeekClient"
    }

    @Bean
    fun retryRegistry(): RetryRegistry {
        val deepSeekRetryConfig = RetryConfig.custom<List<DeepSeekResponse>>()
            .maxAttempts(5)
            .retryExceptions(IOException::class.java, WebClientRequestException::class.java)
            .intervalFunction(IntervalFunction.ofExponentialBackoff(100, 2.0))
            .build()

        return RetryRegistry.of(
            mapOf(
                DEEPSEEK_CLIENT to deepSeekRetryConfig
            )
        )
    }

    @Bean
    fun retryLogger(retryRegistry: RetryRegistry) = RetryLogger(retryRegistry)
}

class RetryLogger(
    retryRegistry: RetryRegistry
) {
    companion object {
        private val logger = logger()
    }

    init {
        retryRegistry
            .allRetries
            .map { retry ->
                retry
                    .eventPublisher
                    .onRetry {
                        logger.info("Retrying: $it")
                    }
            }
    }
}
