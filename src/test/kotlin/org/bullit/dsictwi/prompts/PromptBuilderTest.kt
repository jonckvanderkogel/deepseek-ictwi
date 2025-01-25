package org.bullit.dsictwi.prompts

import kotlinx.coroutines.test.runTest
import org.bullit.dsictwi.config.PromptConfig
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class PromptBuilderTest {
    private lateinit var promptConfig: PromptConfig
    private lateinit var promptBuilder: PromptBuilder

    @BeforeEach
    fun setup() {
        promptConfig = PromptConfig(
            system = PromptConfig.SystemPromptConfig(
                base = "Base system prompt",
                rules = listOf(
                    "First rule",
                    "Second rule",
                    "Third rule"
                ),
                contextHeader = "CONTEXT HEADER:"
            ),
            user = PromptConfig.UserPromptConfig(
                instructions = listOf(
                    "First instruction",
                    "Second instruction"
                ),
                exampleFormat = "EXAMPLE %d:\nPL:%s\nJAVA:%s",
                targetHeader = "TARGET HEADER:"
            )
        )

        promptBuilder = PromptBuilder(promptConfig)
    }

    @Test
    fun `buildSystemPrompt should combine base rules and context`() = runTest {
        val contextJava = listOf(
            "Context line 1",
            "Context line 2"
        )

        val result = promptBuilder.buildSystemPrompt(contextJava)

        val expectedContent = """
            Base system prompt
                
            Rules:
            • First rule
            • Second rule
            • Third rule
                
            CONTEXT HEADER:
            // Context API:
            Context line 1
            
            // Context API:
            Context line 2
        """.trimIndent()

        assertEquals("system", result.role)
        assertEquals(expectedContent.normalize(), result.content.normalize())
    }

    @Test
    fun `buildUserPrompt should format examples and target correctly`() = runTest {
        val examples = listOf(
            CodePair("PL1", "JAVA1"),
            CodePair("PL2", "JAVA2")
        )
        val targetPlsql = "TARGET_PLSQL"

        val result = promptBuilder.buildUserPrompt(examples, targetPlsql)

        val expectedContent = """
            First instruction
            Second instruction
                
            EXAMPLE 1:
            PL:PL1
            JAVA:JAVA1
            
            EXAMPLE 2:
            PL:PL2
            JAVA:JAVA2
                
            TARGET HEADER:
            TARGET_PLSQL
        """.trimIndent()

        assertEquals("user", result.role)
        assertEquals(expectedContent.normalize(), result.content.normalize())
    }

    @Test
    fun `buildUserPrompt should handle empty examples list`() = runTest {
        val result = promptBuilder.buildUserPrompt(emptyList(), "TARGET")

        val expectedContent = """
            First instruction
            Second instruction
                
            
                
            TARGET HEADER:
            TARGET
        """.trimIndent()

        assertEquals(expectedContent.normalize(), result.content.normalize())
    }

    @Test
    fun `buildSystemPrompt should handle empty context`() = runTest {
        val result = promptBuilder.buildSystemPrompt(emptyList())

        val expectedContent = """
            Base system prompt
                
            Rules:
            • First rule
            • Second rule
            • Third rule
                
            CONTEXT HEADER:
        """.trimIndent()

        assertEquals(expectedContent.normalize(), result.content.normalize())
    }

    private fun String.normalize() = this
        .lines()
        .joinToString("\n") { it.trim() } // Trim each line
        .trim()
}