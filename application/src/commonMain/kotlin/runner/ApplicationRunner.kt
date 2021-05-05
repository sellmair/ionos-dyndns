package io.sellmair.ionos.dyndns.runner

import io.sellmair.ionos.dyndns.api.PublicIpProvider
import io.sellmair.ionos.dyndns.model.*
import io.sellmair.ionos.dyndns.update.DnsRecordUpdateFailure
import io.sellmair.ionos.dyndns.update.DnsRecordUpdateResult
import io.sellmair.ionos.dyndns.update.toDiagnostic
import io.sellmair.ionos.dyndns.update.updateDnsRecord
import io.sellmair.ionos.dyndns.util.Logger.logError
import io.sellmair.ionos.dyndns.util.Logger.logFatal
import io.sellmair.ionos.dyndns.util.Logger.logInfo
import io.sellmair.ionos.dyndns.util.Logger.logPendingInfo
import io.sellmair.ionos.dyndns.util.leftOr
import kotlinx.coroutines.runBlocking
import kotlin.time.Duration


fun ApplicationRunner(mode: ApplicationArguments.Mode): ApplicationRunner {
    return when (mode) {
        is ApplicationArguments.Mode.SingleInvocation -> SingleInvocationApplicationRunner
        is ApplicationArguments.Mode.Daemon -> DaemonApplicationRunner(mode.ipRefreshInterval)
    }
}

sealed interface ApplicationRunner {
    operator fun invoke(ipProvider: PublicIpProvider, configurationFile: DomainsConfigurationFile)
}

data class DaemonApplicationRunner(val ipRefreshInterval: Duration) : ApplicationRunner {
    override fun invoke(ipProvider: PublicIpProvider, configurationFile: DomainsConfigurationFile) {
        TODO("Not yet implemented")
    }

}

object SingleInvocationApplicationRunner : ApplicationRunner {
    override fun invoke(ipProvider: PublicIpProvider, configurationFile: DomainsConfigurationFile) {
        runBlocking {
            val ipAddress = ipProvider() ?: error("Failed resolving own ip-address from $ipProvider")
            logInfo("Resolved ip-address from $ipProvider: $ipAddress")

            val domains = configurationFile.readDomains().leftOr { failure ->
                logFatal(failure.toDiagnostic())
            }

            domains.map(DomainDTO::toDomain).forEach { domain ->
                logPendingInfo("Updating ${domain.targetDomainName}... ")
                when (val result = updateDnsRecord(domain, ipAddress)) {
                    is DnsRecordUpdateResult.Success -> logInfo("DONE ☑️")
                    is DnsRecordUpdateFailure -> logFatal(result.toDiagnostic(domain))
                }
            }
        }
    }
}
