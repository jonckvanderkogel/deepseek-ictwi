package org.bullit.dsictwi.prompts

import org.bullit.dsictwi.config.PromptConfig
import org.springframework.stereotype.Service

private val sep = System.lineSeparator()
private val sep2 = sep + sep

@Service
class PromptBuilder(private val config: PromptConfig) {

    fun buildSystemPrompt(contextJava: List<String>): Message =
        Message(
            "system",
            """
                ${config.system.base}
                
                Rules:
                ${config.system.rules.joinToString(sep) { "• $it" }}
                
                ${config.system.contextHeader}
                ${contextJava.joinToString(sep2) { "// Context API:$sep$it" }}
            """.trimIndent()
        )

    fun buildUserPrompt(examples: List<CodePair>, targetPlsql: String): Message {
        val examplesContent = examples.mapIndexed { index, pair ->
            config.user.exampleFormat.format(
                index + 1,
                pair.plsql.trim(),
                pair.java.trim()
            )
        }.joinToString(sep2)

        return Message(
            "user",
            """
                ${config.user.instructions.joinToString(sep)}
                
                $examplesContent
                
                ${config.user.targetHeader}
                $targetPlsql
            """.trimIndent()
        )
    }
}