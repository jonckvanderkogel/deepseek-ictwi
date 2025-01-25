package org.bullit.dsictwi

import arrow.core.Either
import arrow.core.left
import arrow.core.nonEmptyListOf
import arrow.core.right
import kotlinx.coroutines.test.runTest
import org.bullit.dsictwi.error.ApiError
import org.bullit.dsictwi.error.ApplicationError
import org.bullit.dsictwi.error.ApplicationErrors
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import reactor.core.publisher.Mono
import java.io.FileNotFoundException

class UtilTest {
    private val errorHandler: (Throwable) -> ApplicationError = { throwable ->
        when (throwable) {
            is FileNotFoundException -> ApiError("File error occurred")
            else -> ApiError(throwable.message ?: "Unknown error")
        }
    }

    @Test
    fun `success case returns Right with value`() = runTest {
        val testValue = "success"
        val result = Mono.just(testValue)
            .toEither(errorHandler)

        assertEquals(testValue.right(), result)
    }

    @Test
    fun `error case returns Left with transformed error`() = runTest {
        val testError = RuntimeException("database failure")
        val result = Mono.error<String>(testError)
            .toEither(errorHandler)

        assertEquals(ApiError("database failure").left(), result)
    }

    @Test
    fun `empty Mono with Unit type returns Right Unit`() = runTest {
        val result = Mono.empty<Unit>()
            .toEither(errorHandler)

        assertEquals(Unit.right(), result)
    }

    @Test
    fun `empty Mono with non-Unit type returns ApiError`() = runTest {
        val result = Mono.empty<String>()
            .toEither(errorHandler)

        assertEquals(ApiError("No response").left(), result)
    }

    @Test
    fun `custom error mapping based on exception type`() = runTest {
        val testError = FileNotFoundException("missing.txt")
        val result = Mono.error<Int>(testError)
            .toEither(errorHandler)

        assertEquals(ApiError("File error occurred").left(), result)
    }

    private val error1 = ApiError("Error 1")
    private val error2 = ApiError("Error 2")
    private val error3 = ApiError("Error 3")

    @Test
    fun `all rights should return right with list of values`() {
        val input = listOf(
            Either.Right("value1"),
            Either.Right("value2"),
            Either.Right("value3")
        )

        val result = input.flatten()
        assertEquals(Either.Right(listOf("value1", "value2", "value3")), result)
    }

    @Test
    fun `single left should return left with contained errors`() {
        val input = listOf(
            Either.Left(nonEmptyListOf(error1)),
            Either.Right("value"),
            Either.Left(nonEmptyListOf(error2))
        )

        val result = input.flatten()
        assertEquals(Either.Left(nonEmptyListOf(error1, error2)), result)
    }

    @Test
    fun `multiple lefts should aggregate all errors`() {
        val input = listOf(
            Either.Left(nonEmptyListOf(error1, error2)),
            Either.Left(nonEmptyListOf(error3))
        )

        val result = input.flatten()
        assertEquals(Either.Left(nonEmptyListOf(error1, error2, error3)), result)
    }

    @Test
    fun `mixed lefts and rights should prioritize errors`() {
        val input = listOf(
            Either.Right("value1"),
            Either.Left(nonEmptyListOf(error1)),
            Either.Right("value2"),
            Either.Left(nonEmptyListOf(error2, error3))
        )

        val result = input.flatten()
        assertEquals(Either.Left(nonEmptyListOf(error1, error2, error3)), result)
    }

    @Test
    fun `empty list should return right with empty list`() {
        val input = emptyList<Either<ApplicationErrors, String>>()
        val result = input.flatten()
        assertEquals(Either.Right(emptyList<String>()), result)
    }

    @Test
    fun `should maintain error order from original list`() {
        val input = listOf(
            Either.Left(nonEmptyListOf(error1)),
            Either.Left(nonEmptyListOf(error2)),
            Either.Left(nonEmptyListOf(error3))
        )

        val result = input.flatten()
        assertEquals(
            Either.Left(nonEmptyListOf(error1, error2, error3)),
            result
        )
    }
}