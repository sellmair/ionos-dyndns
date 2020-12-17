package io.sellmair.ionos.dyndns.service

import io.sellmair.ionos.dyndns.cli.DomainConfiguration
import io.sellmair.ionos.dyndns.cli.DomainConfigurationDTO
import io.sellmair.ionos.dyndns.cli.toDuration
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlin.time.Duration

@Serializable
data class ServiceConfigurationDTO(
    val ipRefreshInterval: String,
    val domains: List<DomainConfigurationDTO>
) {
    fun toServiceConfiguration() = ServiceConfiguration(
        ipRefreshInterval.toDuration(),
        domains = domains.map { it.toArguments() }
    )

    fun toJson(): String {
        return Json.encodeToString(serializer(), this)
    }

    companion object {
        fun fromJson(json: String): ServiceConfigurationDTO {
            return Json.decodeFromString(serializer(), json)
        }
    }
}

data class ServiceConfiguration(
    val ipRefreshInterval: Duration,
    val forceRefreshInterval: Duration? = null,
    val domains: List<DomainConfiguration> = emptyList()
)

