package io.sellmair.ionos.dyndns.actions

import io.sellmair.ionos.dyndns.ConfigFile
import io.sellmair.ionos.dyndns.DyndnsDomains
import io.sellmair.ionos.dyndns.api.*
import io.sellmair.ionos.dyndns.api.ApiResult
import io.sellmair.ionos.dyndns.api.CreatedDyndnsBulkDTO
import io.sellmair.ionos.dyndns.api.DyndnsBulkDTO
import io.sellmair.ionos.dyndns.api.withApi
import io.sellmair.ionos.dyndns.exit
import io.sellmair.ionos.dyndns.util.Logger
import io.sellmair.ionos.dyndns.util.leftOr

fun setupDyndns(configFile: ConfigFile, domains: List<String>) {
    Logger.logInfo("ionos-dyndns is not yet setup")
    Logger.logInfo("Please enter the API key and hit enter")
    val apiKey = readln()

    Logger.logInfo("Please enter dyndns description (optional)")
    val description = readln().takeIf { it.isNotBlank() } ?: "ionos-dyndns"

    when (val result = withApi(apiKey) { createDyndnsBulk(DyndnsBulkDTO(description, domains)) }) {
        is ApiResult.Success -> storeNewBulk(configFile, apiKey, result.value)
        is ApiResult.Failure -> exit(result)
    }
}

private fun storeNewBulk(configFile: ConfigFile, apiKey: String, createdDyndnsBulkDTO: CreatedDyndnsBulkDTO) {
    configFile.write(
        DyndnsDomains(
            apiKey = apiKey,
            bulkId = createdDyndnsBulkDTO.bulkId,
            updateUrl = createdDyndnsBulkDTO.updateUrl,
            description = createdDyndnsBulkDTO.description,
            domains = createdDyndnsBulkDTO.domains.toSet()
        )
    ).leftOr(::exit)
}
