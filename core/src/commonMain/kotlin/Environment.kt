package io.sellmair.ionos.dyndns.cli

fun environmentVariable(key: String): String {
    return environmentVariableOrNull(key) ?: throw MissingEnvironmentVariableException(key)
}

expect fun environmentVariableOrNull(key: String): String?

class MissingEnvironmentVariableException(key: String): Exception("Missing environment variable: $key")
