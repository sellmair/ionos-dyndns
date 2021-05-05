package io.sellmair.ionos.dyndns.util

object Logger {

    data class Diagnostic(val title: String, val details: String? = null, val throwable: Throwable? = null)

    fun logPendingInfo(message: String) = print(message)

    fun logInfo(message: String) = println(message)

    fun logError(diagnostic: Diagnostic) {
        logError(message = diagnostic.title, details = diagnostic.details)
    }

    fun logFatal(diagnostic: Diagnostic): Nothing {
        logError(diagnostic)
        throw Throwable(cause = diagnostic.throwable)
    }

    fun logError(message: String, details: String? = null) {
        println(message.prependIndent(" > "))
        if (details != null) {
            println(details.prependIndent(" >  >  "))
        }
    }
}
