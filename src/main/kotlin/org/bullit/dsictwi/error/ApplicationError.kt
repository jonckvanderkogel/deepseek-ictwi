package org.bullit.dsictwi.error

import arrow.core.NonEmptyList

typealias ApplicationErrors = NonEmptyList<ApplicationError>

sealed interface ApplicationError {
    val message: String
    val throwable: Throwable?
}

abstract class AbstractApplicationError(
    override val message: String,
    override val throwable: Throwable? = null
) : ApplicationError {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as AbstractApplicationError

        if (message != other.message) return false
        return throwable == other.throwable
    }

    override fun hashCode(): Int {
        var result = message.hashCode()
        result = 31 * result + (throwable?.hashCode() ?: 0)
        return result
    }

    override fun toString(): String {
        return "${this::class.simpleName}(message='$message', throwable=$throwable)"
    }
}

class InvalidInputNumber(inputNumber: Int) : AbstractApplicationError(
    message = "Input number $inputNumber needs to be between 1 and 10"
)

class FileNotFound(filename: String, throwable: Throwable?) : AbstractApplicationError(
    message = "File $filename not found",
    throwable = throwable
)

class ApiError(message: String = "Error interacting with the DeepSeek API") : AbstractApplicationError(
    message = message
)

class MissingDocumentVector(document: String) : AbstractApplicationError(
    message = "Missing document vector for $document"
)

fun <T : ApplicationError> NonEmptyList<T>.joinMessages(separator: CharSequence = ", "): String =
    this.map { it }.joinToString(separator = separator)
