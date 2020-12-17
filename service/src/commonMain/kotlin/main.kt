
package io.sellmair.ionos.dyndns.service

import io.sellmair.ionos.dyndns.cli.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking

fun main(args: Array<String>) {
    val configuration = readServiceConfigurationOrNull(args) ?: return onMissingServiceConfiguration()
    runBlocking {
        runService(configuration)
    }
}

suspend fun runService(configuration: ServiceConfiguration) {
    val ipAdressByDomain = mutableMapOf<Domain, IpAddress>()
    while (true) {
        for (domainConfiguraiton in configuration.domains) {
            val domain = domainConfiguraiton.toDomain()
            val currentIp = ipAdressByDomain[domain]
            val ip = domainConfiguraiton.ipProvider()
            if (ip == null) {
                println("Failed to get ip from ${domainConfiguraiton.ipProvider}")
                continue
            }
            if (ip == currentIp) {
                continue
            }

            print("Updating ${domain.targetDomainName}... ")
            val result = updateDnsRecord(domain, ip)
            println("[$result]")

            if (result is DnsRecordUpdateResult.Success) {
                ipAdressByDomain[domain] = ip
            }
        }
        delay(configuration.ipRefreshInterval)
    }
}
