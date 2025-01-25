package org.bullit.dsictwi.prompts

import arrow.core.Either
import arrow.core.nel
import arrow.core.raise.catch
import arrow.core.raise.either
import org.bullit.dsictwi.error.ApplicationErrors
import org.bullit.dsictwi.error.FileNotFound
import org.bullit.dsictwi.flatten
import org.springframework.core.io.Resource
import org.springframework.core.io.support.ResourcePatternResolver
import org.springframework.stereotype.Service

@Service
class CodeReader(
    private val resourcePatternResolver: ResourcePatternResolver
) {
    fun getContextJava() = cachedContexts

    fun getCodePairs() = cachedCodePairs

    private val cachedContexts by lazy {
        loadContextsSafely()
    }

    private val cachedCodePairs by lazy {
        readCodePairs()
    }

    private fun loadContextsSafely(): Either<ApplicationErrors, List<String>> = either {
        catch({
            resourcePatternResolver.getResources("classpath*:context/*.txt")
                .sortedBy { it.filename }
                .map { it.readText() }
        }) { throwable ->
            raise(
                FileNotFound(
                    "Context file",
                    throwable
                ).nel()
            )
        }
    }

    private fun readCodePairs(): Either<ApplicationErrors, List<CodePair>> =
        (1..10)
            .map { readCodePair(it) }
            .flatten()

    private fun Resource.readText(): String =
        inputStream.bufferedReader().use { it.readText() }

    private fun readCodePair(number: Int): Either<ApplicationErrors, CodePair> = Either.catch {
        val plsql = readResource("plsql-$number.txt")
        val java = readResource("java-$number.txt")
        CodePair(plsql, java)
    }.mapLeft { FileNotFound("Pair $number", it).nel() }

    private fun readResource(filename: String): String =
        resourcePatternResolver
            .getResources("classpath:samples/$filename")[0]
            .inputStream
            .bufferedReader()
            .use { it.readText() }
}
