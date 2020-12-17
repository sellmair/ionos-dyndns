package io.sellmair.ionos.dyndns.cli

import kotlinx.serialization.Serializable
import kotlin.time.Duration

data class DomainConfiguration(
    val apiKey: String,
    val rootDomainName: String,
    val targetDomainName: String,
    val timeToLive: Duration = defaultTimeToLive,
    val ipProvider: PublicIpProvider = defaultIpProvider
)

data class Domain(
    val apiKey: String,
    val rootDomainName: String,
    val targetDomainName: String,
    val timeToLive: Duration
)

@Serializable
internal data class ZoneDescriptor(val name: String, val id: String, val type: String)

@Serializable
internal data class Zone(val name: String, val id: String, val records: List<DnsRecord>)

@Serializable
internal data class DnsRecord(
    val id: String,
    val name: String,
    val rootName: String,
    val type: String,
    val content: String,
    val ttl: Int,
    val disabled: Boolean,
    val prio: Int = 0,
)

@Serializable
internal data class DnsRecordUpdate(
    val content: String,
    val ttl: Int,
    val disabled: Boolean,
    val prio: Int
)

