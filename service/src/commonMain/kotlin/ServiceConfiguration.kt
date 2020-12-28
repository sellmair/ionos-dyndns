package io.sellmair.ionos.dyndns.service

import io.sellmair.ionos.dyndns.cli.DomainConfiguration
import io.sellmair.ionos.dyndns.cli.DomainConfigurationDTO
import io.sellmair.ionos.dyndns.cli.toDuration
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlin.time.Duration


fun String.toServiceMode(): ServiceMode {
    return when (toLowerCase().trim()) {
        "daemon", "deamon", "d" -> ServiceMode.Daemon
        "persistent", "p" -> ServiceMode.Persistent
        else -> throw IllegalArgumentException("Unknown ServiceMode: $this")
    }
}

enum class ServiceMode {
    Daemon, Persistent
}

@Serializable
data class ServiceConfigurationDTO(
    val mode: String = ServiceMode.Persistent.name,
    val ipRefreshInterval: String,
    val domains: List<DomainConfigurationDTO>
) {
    fun toServiceConfiguration() = ServiceConfiguration(
        mode = mode.toServiceMode(),
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
    val mode: ServiceMode,
    val ipRefreshInterval: Duration,
    val domains: List<DomainConfiguration> = emptyList()
)

