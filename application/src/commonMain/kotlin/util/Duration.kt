@file:Suppress("MoveVariableDeclarationIntoWhen")


package io.sellmair.ionos.dyndns.util

import kotlin.time.*
import kotlin.time.Duration.Companion.days
import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds

private val durationRegex = Regex("""\d+(\.\d+)?[smhd]""")

fun String.toDurationOrNull(): Duration? {
    if (!this.matches(durationRegex)) {
        return null
    }

    val value = this.dropLast(1).toDouble()
    val unit = this.last().toString()

    return when (unit) {
        "s" -> value.seconds
        "m" -> value.minutes
        "h" -> value.hours
        "d" -> value.days
        else -> throw IllegalStateException()
    }
}

fun String.toDuration(): Duration = toDurationOrNull() ?: throw IllegalArgumentException(
    "Cannot parse \"$this\" as Duration"
)
