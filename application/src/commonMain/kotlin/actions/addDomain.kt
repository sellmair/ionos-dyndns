package io.sellmair.ionos.dyndns.actions

import io.sellmair.ionos.dyndns.ConfigFile
import io.sellmair.ionos.dyndns.api.DyndnsBulkDTO
import io.sellmair.ionos.dyndns.api.withApi
import io.sellmair.ionos.dyndns.exit
import io.sellmair.ionos.dyndns.util.Logger.logInfo
import io.sellmair.ionos.dyndns.util.leftOr

fun addDomain(domain: String) {
    logInfo("Adding domain: $domain")
    val configFile = ConfigFile.inUserHome

    val currentDyndnsDomains = configFile.read().leftOr { readingFailure ->
        /* */
        return if (readingFailure is ConfigFile.FileNotFound)
            setupDyndns(configFile, listOf(domain)) else exit(readingFailure)
    }

    withApi(currentDyndnsDomains.apiKey) {
        updateDyndnsBulk(
            currentDyndnsDomains.bulkId, DyndnsBulkDTO(
                description = currentDyndnsDomains.description,
                domains = (currentDyndnsDomains.domains + domain).toList()
            )
        ).successOrThrow()
    }

    val updatedDyndnsDomains = currentDyndnsDomains.copy(
        domains = currentDyndnsDomains.domains + domain
    )

    configFile.write(updatedDyndnsDomains).leftOr(::exit)

    logInfo("Added $domain")
}

