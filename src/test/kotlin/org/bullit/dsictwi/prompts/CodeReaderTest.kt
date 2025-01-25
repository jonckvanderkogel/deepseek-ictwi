package org.bullit.dsictwi.prompts

import org.bullit.dsictwi.error.FileNotFound
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.mockito.Mockito.mock
import org.mockito.Mockito.`when`
import org.springframework.core.io.Resource
import org.springframework.core.io.support.ResourcePatternResolver
import java.io.ByteArrayInputStream

class CodeReaderTest {

    private val mockResolver = mock(ResourcePatternResolver::class.java)
    private val codeReader = CodeReader(mockResolver)

    @Test
    fun `getContextJava returns contexts when files exist`() {
        val context1 = mockResource("context1.txt", "Context API 1")
        val context2 = mockResource("context2.txt", "Context API 2")

        `when`(mockResolver.getResources("classpath*:context/*.txt"))
            .thenReturn(arrayOf(context1, context2))

        val result = codeReader.getContextJava()

        assertTrue(result.isRight())
        assertEquals(listOf("Context API 1", "Context API 2"), result.getOrNull())
    }

    @Test
    fun `getContextJava returns FileNotFound error when context loading fails`() {
        `when`(mockResolver.getResources("classpath*:context/*.txt"))
            .thenThrow(RuntimeException("File not found"))

        val result = codeReader.getContextJava()

        assertTrue(result.isLeft())
        val error = result.leftOrNull()?.head
        assertTrue(error is FileNotFound)
        assertEquals("File Context file not found", error?.message)
    }

    @Test
    fun `getCodePairs returns 10 code pairs when all resources exist`() {
        // Mock 10 valid code pairs
        (1..10).forEach { number ->
            val plsql = mockResource("plsql-$number.txt", "PL/SQL $number")
            val java = mockResource("java-$number.txt", "Java $number")

            `when`(mockResolver.getResources("classpath:samples/plsql-$number.txt"))
                .thenReturn(arrayOf(plsql))
            `when`(mockResolver.getResources("classpath:samples/java-$number.txt"))
                .thenReturn(arrayOf(java))
        }

        val result = codeReader.getCodePairs()

        assertTrue(result.isRight())
        assertEquals(10, result.getOrNull()?.size)
        result.getOrNull()?.forEachIndexed { index, pair ->
            assertEquals("PL/SQL ${index + 1}", pair.plsql)
            assertEquals("Java ${index + 1}", pair.java)
        }
    }

    @Test
    fun `getCodePairs returns error when a code pair resource is missing`() {
        // Mock 9 valid pairs and 1 invalid (plsql-5.txt missing)
        (1..10).forEach { number ->
            if (number != 5) {
                val plsql = mockResource("plsql-$number.txt", "PL/SQL $number")
                val java = mockResource("java-$number.txt", "Java $number")

                `when`(mockResolver.getResources("classpath:samples/plsql-$number.txt"))
                    .thenReturn(arrayOf(plsql))
                `when`(mockResolver.getResources("classpath:samples/java-$number.txt"))
                    .thenReturn(arrayOf(java))
            } else {
                // Simulate missing plsql-5.txt
                `when`(mockResolver.getResources("classpath:samples/plsql-5.txt"))
                    .thenThrow(RuntimeException("File not found"))
            }
        }

        val result = codeReader.getCodePairs()

        assertTrue(result.isLeft())
        val error = result.leftOrNull()
        assertTrue(error?.any { it is FileNotFound && it.message.contains("Pair 5") } == true)
    }

    private fun mockResource(filename: String, content: String): Resource {
        val resource = mock(Resource::class.java)
        `when`(resource.filename).thenReturn(filename)
        `when`(resource.inputStream).thenReturn(ByteArrayInputStream(content.toByteArray()))
        return resource
    }
}