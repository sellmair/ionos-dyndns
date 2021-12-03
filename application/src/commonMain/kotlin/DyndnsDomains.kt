package io.sellmair.ionos.dyndns

import kotlinx.serialization.Serializable

@Serializable
data class DyndnsDomains(
    val apiKey: String,
    val bulkId: String,
    val updateUrl: String,
    val description: String,
    val domains: Set<String>
)
