package org.bullit.dsictwi.prompts

import arrow.core.Either
import org.bullit.dsictwi.config.DeepSeekConfig
import org.bullit.dsictwi.error.ApiError
import org.bullit.dsictwi.error.ApplicationErrors
import org.bullit.dsictwi.toApplicationErrors
import org.bullit.dsictwi.toEither
import org.springframework.stereotype.Service
import org.springframework.web.reactive.function.client.WebClient
import reactor.core.publisher.Mono

@Service
class DeepSeekClient(
    private val config: DeepSeekConfig,
    private val webClient: WebClient
) {
    suspend fun generateCode(messages: List<Message>): Either<ApplicationErrors, DeepSeekResponse> =
        generateCodeReactive(messages)
            .toEither { t -> ApiError(t.message ?: "Error interacting with DeepSeek API") }
            .toApplicationErrors()

    private fun generateCodeReactive(messages: List<Message>): Mono<DeepSeekResponse> {
        val request = DeepSeekRequest(
            messages = messages,
            model = config.model,
            temperature = config.temperature
        )

        return webClient
            .post()
            .uri(config.chatUrl)
            .bodyValue(request)
            .retrieve()
            .bodyToMono(DeepSeekResponse::class.java)
    }

    private data class DeepSeekRequest(
        val messages: List<Message>,
        val model: String,
        val temperature: Double
    )

    data class DeepSeekResponse(
        val choices: List<Choice>
    ) {
        data class Choice(
            val index: Int,
            val message: Message
        )
    }
}