package org.bullit.dsictwi

import org.bullit.dsictwi.wiremock.WireMockProxy
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.web.reactive.server.WebTestClient
import kotlin.time.Duration.Companion.minutes
import kotlin.time.toJavaDuration
import org.hamcrest.Matchers.containsString

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureWebTestClient
class CodeGenControllerIntegrationTest(
    @Autowired private val proxy: WireMockProxy
) : AbstractWiremockTest(proxy) {

    @Autowired
    lateinit var webTestClient: WebTestClient

    private fun createClientWithTimeout(): WebTestClient = webTestClient.mutate()
        .responseTimeout(5.minutes.toJavaDuration())
        .build()

    @Test
    fun `generateCode with input 7 should return OK using similarity search`() {
        val customClient = createClientWithTimeout()

        customClient.get()
            .uri("/generate/7")
            .exchange()
            .expectStatus().isOk
            .expectBody()
            .jsonPath("$.choices[0].message.content")
            .exists()
    }

    @Test
    fun `generateCode with input 7 and use-all-examples should return OK`() {
        val customClient = createClientWithTimeout()

        customClient.get()
            .uri("/generate/7?use-all-examples=true")
            .exchange()
            .expectStatus().isOk
            .expectBody()
            .jsonPath("$.choices[0].message.content")
            .exists()
    }

    @Test
    fun `generateCode with invalid number should return BadRequest`() {
        webTestClient.get()
            .uri("/generate/42")  // Number out of range
            .exchange()
            .expectStatus().isBadRequest
            .expectBody()
            .jsonPath("$.message")
            .value(containsString("Input number 42 needs to be between 1 and 10"))
    }
}