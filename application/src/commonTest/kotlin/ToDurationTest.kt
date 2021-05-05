package io.sellmair.ionos.dyndns

import io.sellmair.ionos.dyndns.util.toDuration
import io.sellmair.ionos.dyndns.util.toDurationOrNull
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNull
import kotlin.time.Duration.Companion.days
import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds


class ToDurationTest {

    @Test
    fun seconds() {
        assertEquals(1.seconds, "1s".toDuration())
        assertEquals(1.5.seconds, "1.5s".toDuration())
    }

    @Test
    fun minutes() {
        assertEquals(1.minutes, "1m".toDuration())
        assertEquals(1.5.minutes, "1.5m".toDuration())
    }

    @Test
    fun hours() {
        assertEquals(1.hours, "1h".toDuration())
        assertEquals(1.5.hours, "1.5h".toDuration())
    }

    @Test
    fun days() {
        assertEquals(1.days, "1d".toDuration())
        assertEquals(1.5.days, "1.5d".toDuration())
    }

    @Test
    fun illegalArgument() {
        assertFailsWith<IllegalArgumentException> { "1".toDuration() }
        assertFailsWith<IllegalArgumentException> { "".toDuration() }
        assertFailsWith<IllegalArgumentException> { "1sm".toDuration() }

        assertNull("1".toDurationOrNull())
        assertNull("".toDurationOrNull())
        assertNull("1dm".toDurationOrNull())
    }
}
