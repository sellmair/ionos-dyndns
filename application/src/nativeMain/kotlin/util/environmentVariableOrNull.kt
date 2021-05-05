package io.sellmair.ionos.dyndns.util

import kotlinx.cinterop.toKString
import platform.posix.getenv

actual fun environmentVariableOrNull(key: String): String? {
    return getenv(key)?.toKString()
}
