package io.sellmair.ionos.dyndns.model

import io.sellmair.ionos.dyndns.util.toDuration
import kotlinx.serialization.Serializable
import kotlin.time.Duration
import kotlin.time.Duration.Companion.hours

data class Domain(
    val apiKey: String,
    val rootDomainName: String,
    val targetDomainName: String,
    val timeToLive: Duration
)

@Serializable
data class DomainDTO(
    val apiKey: String,
    val rootDomainName: String,
    val targetDomainName: String,
    val timeToLive: String?
) {
    companion object {
        val defaultTimeToLive = 1.hours
    }
}

fun DomainDTO.toDomain() = Domain(
    apiKey = apiKey,
    rootDomainName = rootDomainName,
    targetDomainName = targetDomainName,
    timeToLive = timeToLive?.toDuration() ?: DomainDTO.defaultTimeToLive
)