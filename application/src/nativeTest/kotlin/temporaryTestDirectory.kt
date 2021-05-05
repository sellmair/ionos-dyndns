package io.sellmair.ionos.dyndns

import io.ktor.utils.io.core.*
import io.sellmair.ionos.dyndns.util.File
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.toKString
import kotlinx.cinterop.usePinned
import platform.posix.mkdtemp

actual val temporaryTestDirectory: File
    get() {
        val template = "/tmp/ionos-dyndns.test.XXXXXXXX".toByteArray()
        val pathPtr = template.usePinned {
            mkdtemp(it.addressOf(0))
        } ?: error("Failed to create temporary test directory")
        return File(pathPtr.toKString())
    }