package io.sellmair.ionos.dyndns.actions

import io.sellmair.ionos.dyndns.actions.RefreshDomainFailure.ConfigFileReadingFailure
import io.sellmair.ionos.dyndns.actions.RefreshDomainFailure.UnknownFailure
import io.sellmair.ionos.dyndns.util.Logger
import io.sellmair.ionos.dyndns.util.Logger.logInfo
import io.sellmair.ionos.dyndns.util.leftOr
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import kotlin.time.Duration

fun refreshDomainsDaemon(interval: Duration) {
    logInfo("Starting dyndns-daemon")
    logInfo("Refresh Interval: $interval")
    runBlocking {
        while (true) {
            refreshDomains().leftOr { failure ->
                when (failure) {
                    is ConfigFileReadingFailure -> Logger.logWarn("Failed reading config file: ${failure.underlying}")
                    is UnknownFailure -> Logger.logWarn("Unknown failure: ${failure.reason.stackTraceToString()}")
                }
            }

            delay(interval)
        }
    }
}
