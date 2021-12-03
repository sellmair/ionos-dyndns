package io.sellmair.ionos.dyndns.api

import kotlinx.serialization.Serializable

@Serializable
internal data class DyndnsBulkDTO(
    val description: String,
    val domains: List<String>
)

@Serializable
internal data class CreatedDyndnsBulkDTO(
    val bulkId: String,
    val updateUrl: String,
    val domains: List<String>,
    val description: String
)
