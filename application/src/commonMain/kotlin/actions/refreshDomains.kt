package io.sellmair.ionos.dyndns.actions

import io.ktor.client.*
import io.ktor.client.request.*
import io.ktor.utils.io.core.*
import io.sellmair.ionos.dyndns.ConfigFile
import io.sellmair.ionos.dyndns.exit
import io.sellmair.ionos.dyndns.util.Either
import io.sellmair.ionos.dyndns.util.Logger.logFatal
import io.sellmair.ionos.dyndns.util.Logger.logInfo
import io.sellmair.ionos.dyndns.util.leftOr
import io.sellmair.ionos.dyndns.util.toLeft
import io.sellmair.ionos.dyndns.util.toRight
import kotlinx.coroutines.runBlocking

object RefreshDomainSuccess

sealed class RefreshDomainFailure {
    data class ConfigFileReadingFailure(val underlying: ConfigFile.ReadingFailure) : RefreshDomainFailure()
    data class UnknownFailure(val reason: Throwable) : RefreshDomainFailure()
}

fun refreshDomainsOrExit(): RefreshDomainSuccess {
    return refreshDomains().leftOr { failure ->
        when (failure) {
            is RefreshDomainFailure.ConfigFileReadingFailure -> exit(failure.underlying)
            is RefreshDomainFailure.UnknownFailure -> logFatal(
                "Failed refreshing domains", failure.reason
            )
        }
    }
}

fun refreshDomains(): Either<RefreshDomainSuccess, RefreshDomainFailure> {
    val configFile = ConfigFile.inUserHome

    val currentDyndnsDomains = configFile.read().leftOr { readingFailure ->
        return RefreshDomainFailure.ConfigFileReadingFailure(readingFailure).toRight()
    }

    logInfo("Updating domains (description: ${currentDyndnsDomains.description})")

    return runBlocking {
        HttpClient().use { client ->
            try {
                client.get<Unit>(currentDyndnsDomains.updateUrl)
                currentDyndnsDomains.domains.forEach { domain ->
                    logInfo("Updated: $domain")
                }
                RefreshDomainSuccess.toLeft()
            } catch (t: Throwable) {
                RefreshDomainFailure.UnknownFailure(t).toRight()
            }
        }
    }
}
