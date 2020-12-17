package io.sellmair.ionos.dyndns.cli


fun DomainConfiguration.toDomain(): Domain = Domain(
    apiKey = apiKey,
    rootDomainName = rootDomainName,
    targetDomainName = targetDomainName,
    timeToLive = timeToLive,
)

internal fun DnsRecord.toDnsRecordPatch(): DnsRecordUpdate {
    return DnsRecordUpdate(
        content = this.content,
        ttl = this.ttl,
        disabled = this.disabled,
        prio = this.prio
    )
}

fun Domain.toDomainConfiguration(ipProvider: PublicIpProvider): DomainConfiguration {
    return DomainConfiguration(
        apiKey = apiKey,
        rootDomainName = rootDomainName,
        targetDomainName = targetDomainName,
        timeToLive = timeToLive,
        ipProvider = ipProvider
    )
}
