package io.sellmair.ionos.dyndns.cli

import kotlinx.cli.ArgParser
import kotlinx.cli.ArgType
import kotlinx.cli.required
import kotlinx.serialization.Serializable
import kotlin.time.*
import kotlin.time.Duration.Companion.hours

internal val defaultTimeToLive = 1.hours

internal val defaultIpProvider = PublicIpProvider.amazon()


@Serializable
data class DomainConfigurationDTO(
    val apiKey: String,
    val rootDomainName: String,
    val targetDomainName: String,
    val timeToLive: String? = null,
    val ipProvider: String? = null
) {
    fun toArguments(): DomainConfiguration {
        val ipProvider = when {
            ipProvider == null -> defaultIpProvider
            ipProvider == "amazon" -> PublicIpProvider.amazon()
            ipProvider.startsWith("url:") -> PublicIpProvider.fromUrl(ipProvider.removePrefix("url:"))
            ipProvider.startsWith("ip:") -> PublicIpProvider.fromKnownIp(ipProvider.removePrefix("ip:"))
            else -> throw IllegalArgumentException("Unknown Ip Provider value: $ipProvider")
        }

        val timeToLive = timeToLive?.toDuration() ?: defaultTimeToLive

        return DomainConfiguration(
            apiKey = apiKey,
            rootDomainName = rootDomainName,
            targetDomainName = targetDomainName,
            timeToLive = timeToLive,
            ipProvider = ipProvider
        )
    }
}

@OptIn(ExperimentalTime::class)
internal fun parseDomainConfiguration(args: Array<String>): DomainConfiguration {
    val parser = ArgParser("ionos-dyndns")

    val apiKey by parser.option(ArgType.String, "apiKey", "k", "ionos API key").required()

    val rootDomainName by parser.option(ArgType.String, "rootDomainName", "d", "Root domain name").required()

    val targetDomainName by parser.option(ArgType.String, "targetDomainName", "a", "A Record domaiin name").required()

    val timeToLive by parser.option(
        ArgType.String,
        "timeToLive",
        "ttl",
        "Time to live eg. 1s, 1h, 15m",
    )

    val ipProvider by parser.option(ArgType.String, "ipProvider", "ip", "Method of determining own public IP")

    parser.parse(args)

    return DomainConfigurationDTO(
        apiKey = apiKey,
        rootDomainName = rootDomainName,
        targetDomainName = targetDomainName,
        timeToLive = timeToLive,
        ipProvider = ipProvider
    ).toArguments()
}
