package io.sellmair.ionos.dyndns.util

sealed class Either<out Left, out Right>

data class Left<T>(val value: T) : Either<T, Nothing>()
data class Right<T>(val value: T) : Either<Nothing, T>()

val <T> Either<T, *>.leftOrNull
    get() = when (this) {
        is Left -> value
        is Right -> null
    }

val <T> Either<*, T>.rightOrNull
    get() = when (this) {
        is Left -> null
        is Right -> value
    }


inline fun <L, R> Either<L, R>.leftOr(onRight: (R) -> L): L {
    return when (this) {
        is Left -> value
        is Right -> onRight(value)
    }
}

inline fun <L, R, T> Either<L, R>.fold(
    onLeft: (L) -> T,
    onRight: (R) -> T
): T {
    return when (this) {
        is Left -> onLeft(value)
        is Right -> onRight(value)
    }
}

fun <T> T.toLeft() = Left(this)
fun <T> T.toRight() = Right(this)
