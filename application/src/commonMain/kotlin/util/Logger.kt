package io.sellmair.ionos.dyndns.util

object Logger {

    fun logDebug(message: String) = log(LogLevel.Debug, message)

    fun logInfo(message: String) = log(LogLevel.Info, message)

    fun logWarn(message: String) = log(LogLevel.Warn, message)

    fun logError(message: String) = log(LogLevel.Error, message)

    fun logFatal(message: String, throwable: Throwable? = null): Nothing {
        log(LogLevel.Fatal, message)
        throw throwable?: Throwable()
    }

    enum class LogLevel {
        Debug, Info, Warn, Error, Fatal
    }

    fun log(level: LogLevel, message: String, throwable: Throwable? = null) {
        println("[${level.name}] $message")
        if (throwable != null) {
            if (throwable.message != null) {
                println(throwable.message)
            }
            println(throwable.stackTraceToString())
            println()
        }
    }
}
