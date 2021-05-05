package io.sellmair.ionos.dyndns.util

actual fun environmentVariableOrNull(key: String): String? {
    return System.getenv(key)
}
