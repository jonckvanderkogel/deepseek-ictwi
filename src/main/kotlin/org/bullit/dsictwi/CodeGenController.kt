package org.bullit.dsictwi

import arrow.core.Either
import arrow.core.nel
import arrow.core.raise.either
import org.bullit.dsictwi.error.ApplicationErrors
import org.bullit.dsictwi.error.InvalidInputNumber
import org.bullit.dsictwi.error.joinMessages
import org.bullit.dsictwi.prompts.CodeReader
import org.bullit.dsictwi.prompts.DeepSeekClient
import org.bullit.dsictwi.prompts.PromptBuilder
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RestController

@RestController
class CodeGenController(
    private val codeReader: CodeReader,
    private val promptBuilder: PromptBuilder,
    private val deepSeekClient: DeepSeekClient
) {
    @GetMapping("/generate/{number}")
    suspend fun generateCode(@PathVariable number: Int) = either {
        if (number !in 1..10) raise(InvalidInputNumber(number).nel())

        val codePairs = codeReader.getCodePairs().bind()

        val (examples, target) = codePairs.extractElementAt(number - 1)

        val context = codeReader.getContextJava().bind()

        val systemMessage = promptBuilder.buildSystemPrompt(context)
        val userMessage = promptBuilder.buildUserPrompt(examples, target.plsql)

        deepSeekClient.generateCode(listOf(systemMessage, userMessage)).bind()
    }.toHttpResponse()

    private fun <T> List<T>.extractElementAt(index: Int): Pair<List<T>, T> =
        when (index) {
            0 -> subList(1, size) to first()
            lastIndex -> subList(0, lastIndex) to last()
            else -> (subList(0, index) + subList(index + 1, size)) to this[index]
        }

    private fun <T> Either<ApplicationErrors, T>.toHttpResponse() =
        fold(
            ifLeft = {
                ResponseEntity(it.joinMessages(), HttpStatus.BAD_REQUEST)
            },
            ifRight = {
                ResponseEntity(it, HttpStatus.OK)
            }
        )
}