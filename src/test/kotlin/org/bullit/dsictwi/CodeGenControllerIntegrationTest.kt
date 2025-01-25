package org.bullit.dsictwi

import org.bullit.dsictwi.wiremock.WireMockProxy
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.web.reactive.server.WebTestClient
import kotlin.time.Duration.Companion.minutes
import kotlin.time.toJavaDuration

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureWebTestClient
class CodeGenControllerIntegrationTest(
    @Autowired private val proxy: WireMockProxy
) : AbstractWiremockTest(proxy) {

    @Autowired
    lateinit var webTestClient: WebTestClient

    @Test
    fun `generateCode with input 7 should return OK`() {
        // setting higher timeout in case you need to generate the response again.
        // Deepseek API takes quite some time to respond.
        val customClient = webTestClient.mutate()
            .responseTimeout(5.minutes.toJavaDuration())
            .build()

        customClient.get()
            .uri("/generate/7")
            .exchange()
            .expectStatus().isOk
            .expectBody()
            .jsonPath("$.choices[0].message.content").exists()
    }
}