package org.bullit.dsictwi

import arrow.core.Either
import arrow.core.right
import arrow.core.left
import arrow.core.nonEmptyListOf
import arrow.core.toNonEmptyListOrNull
import kotlinx.coroutines.reactive.awaitFirstOrElse
import org.bullit.dsictwi.error.ApiError
import org.bullit.dsictwi.error.ApplicationError
import org.bullit.dsictwi.error.ApplicationErrors
import reactor.core.publisher.Mono
import org.slf4j.Logger
import org.slf4j.LoggerFactory

inline fun <reified T> T.logger(): Logger = LoggerFactory.getLogger(T::class.java)

suspend inline fun <reified T> Mono<T>.toEither(crossinline errorFun: (throwable: Throwable) -> ApplicationError): Either<ApplicationError, T> =
    map<Either<ApplicationError, T>> { it.right() }
        .onErrorResume { Mono.just(errorFun(it).left()) }
        .awaitFirstOrElse {
            if (T::class == Unit::class) {
                (Unit as T).right()
            } else {
                ApiError("No response").left()
            }
        }

fun <T> Either<ApplicationError, T>.toApplicationErrors(): Either<ApplicationErrors, T> =
    mapLeft { nonEmptyListOf(it) }

fun <T> List<Either<ApplicationErrors, T>>.flatten(): Either<ApplicationErrors, List<T>> =
    fold(
        initial = Pair(emptyList<ApplicationError>(), emptyList<T>())
    ) { (errors, values), either ->
        either.fold(
            ifLeft = { error ->
                Pair(errors + error, values)
            },
            ifRight = { value ->
                Pair(errors, values + value)
            }
        )
    }.let { (errors, values) ->
        errors.toNonEmptyListOrNull()?.left() ?: values.right()
    }