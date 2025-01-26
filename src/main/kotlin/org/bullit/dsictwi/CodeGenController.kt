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
import org.bullit.dsictwi.similarity.SimilarityService
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
class CodeGenController(
    private val codeReader: CodeReader,
    private val similarityService: SimilarityService,
    private val promptBuilder: PromptBuilder,
    private val deepSeekClient: DeepSeekClient
) {
    companion object {
        private val logger = logger()
    }

    @GetMapping("/generate/{number}")
    suspend fun generateCode(
        @PathVariable number: Int,
        @RequestParam(name = "use-all-examples", defaultValue = "false") useAllExamples: Boolean
    ) = handleRequest(number, useAllExamples)

    private suspend fun handleRequest(
        number: Int,
        useAllExamples: Boolean
    ) = either {
        if (number !in 1..10) raise(InvalidInputNumber(number).nel())
        val codePairs = codeReader.getCodePairs().bind()
        val (candidates, targetPair) = codePairs.extractElementAt(number - 1)

        val examples = if (useAllExamples) {
            candidates
        } else {
            similarityService.findSimilarExamples(targetPair.plsql, candidates).bind()
        }

        val context = codeReader.getContextJava().bind()
        val systemMessage = promptBuilder.buildSystemPrompt(context)
        val userMessage = promptBuilder.buildUserPrompt(examples, targetPair.plsql)

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
            ifLeft = { errors ->
                ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(mapOf("message" to errors.joinMessages()))
            },
            ifRight = { result ->
                ResponseEntity.ok(result)
            }
        )
}