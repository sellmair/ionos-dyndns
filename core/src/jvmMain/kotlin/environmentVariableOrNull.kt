package io.sellmair.ionos.dyndns.cli

actual fun environmentVariableOrNull(key: String): String? {
    return System.getenv(key)
}
