
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
    val ipAddressByDomain = mutableMapOf<Domain, IpAddress>()
    while (true) {
        for (domainConfiguration in configuration.domains) {
            val domain = domainConfiguration.toDomain()
            val currentIp = ipAddressByDomain[domain]
            val ip = domainConfiguration.ipProvider()
            if (ip == null) {
                println("Failed to get ip from ${domainConfiguration.ipProvider}")
                continue
            }
            if (ip == currentIp) {
                continue
            }

            print("Updating ${domain.targetDomainName}... ")
            val result = updateDnsRecord(domain, ip)
            println("[$result]")

            if (result is DnsRecordUpdateResult.Success) {
                ipAddressByDomain[domain] = ip
            }
        }
        delay(configuration.ipRefreshInterval)
    }
}
