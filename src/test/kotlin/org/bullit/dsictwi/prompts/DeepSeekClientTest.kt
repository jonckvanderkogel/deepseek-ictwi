package org.bullit.dsictwi.prompts

import kotlinx.coroutines.test.runTest
import org.bullit.dsictwi.AbstractWiremockTest
import org.bullit.dsictwi.wiremock.WireMockProxy
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient
import org.springframework.boot.test.context.SpringBootTest

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureWebTestClient
class DeepSeekClientTest(
    @Autowired private val proxy: WireMockProxy,
    @Autowired private val client: DeepSeekClient
) : AbstractWiremockTest(proxy) {

    @Test
    fun `generateCode should return success response for valid API call`() = runTest {
        val response = client
            .generateCode(listOf(
                Message("system", "you are an assistant helping me run my integration test."),
                Message("user", "only return the word 'foo' as a response please, this is for a test.")
            ))

        assertTrue(response.isRight())
        assertEquals("foo", response.getOrNull()?.choices?.get(0)?.message?.content)
    }
}