package org.bullit.dsictwi.prompts

import kotlinx.coroutines.test.runTest
import org.bullit.dsictwi.config.DeepSeekConfig
import org.bullit.dsictwi.error.ApiError
import org.bullit.dsictwi.prompts.DeepSeekClient.DeepSeekResponse
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.mockito.Mockito.*
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.WebClient.*
import reactor.core.publisher.Mono

class DeepSeekClientTest {
    private val mockWebClient: WebClient = mock()
    private val mockRequestSpec: RequestBodyUriSpec = mock()
    private val mockHeadersSpec: RequestHeadersSpec<*> = mock()
    private val mockResponseSpec: ResponseSpec = mock()

    private val config = DeepSeekConfig(
        apiKey = "test-key",
        model = "test-model",
        temperature = 0.7,
        baseUrl = "https://api.deepseek.com",
        chatUrl = "/chat/completions"
    )

    private val client = DeepSeekClient(config, mockWebClient)

    @Test
    fun `generateCode should return success response for valid API call`() = runTest {
        `when`(mockWebClient.post()).thenReturn(mockRequestSpec)
        `when`(mockRequestSpec.uri(config.chatUrl)).thenReturn(mockRequestSpec)
        `when`(mockRequestSpec.bodyValue(any())).thenReturn(mockHeadersSpec)
        `when`(mockHeadersSpec.retrieve()).thenReturn(mockResponseSpec)

        val expectedResponse = DeepSeekResponse(
            listOf(DeepSeekResponse.Choice(0, Message("assistant", "test")))
        )
        `when`(mockResponseSpec.bodyToMono(DeepSeekResponse::class.java))
            .thenReturn(Mono.just(expectedResponse))

        val result = client.generateCode(listOf(Message("user", "test")))

        assertTrue(result.isRight())
        assertEquals(expectedResponse, result.getOrNull())
    }

    @Test
    fun `generateCode should return error for failed API call`() = runTest {
        `when`(mockWebClient.post()).thenReturn(mockRequestSpec)
        `when`(mockRequestSpec.uri(config.chatUrl)).thenReturn(mockRequestSpec)
        `when`(mockRequestSpec.bodyValue(any())).thenReturn(mockHeadersSpec)
        `when`(mockHeadersSpec.retrieve()).thenReturn(mockResponseSpec)
        `when`(mockResponseSpec.bodyToMono(DeepSeekResponse::class.java))
            .thenReturn(Mono.error(RuntimeException("API timeout")))

        val result = client.generateCode(emptyList())

        assertTrue(result.isLeft())
        result.mapLeft { errors ->
            assertTrue(errors.any { it is ApiError && it.message.contains("API timeout") })
        }
    }
}