package io.sellmair.ionos.dyndns.cli

import io.sellmair.ionos.dyndns.cli.DnsRecordUpdateResult.*
import kotlin.math.roundToInt
import kotlin.time.Duration

sealed class DnsRecordUpdateResult {
    data class UnknownError(val throwable: Throwable) : DnsRecordUpdateResult()
    object IonosUnauthorized : DnsRecordUpdateResult()
    object MissingRootDomain : DnsRecordUpdateResult()
    object MissingTargetDomain : DnsRecordUpdateResult()
    object Success : DnsRecordUpdateResult()
}

object FailedToObtainIpAdress

suspend fun updateDnsRecord(configuration: DomainConfiguration):
    Either<DnsRecordUpdateResult, FailedToObtainIpAdress> {
    val ip = configuration.ipProvider() ?: return FailedToObtainIpAdress.toRight()
    return updateDnsRecord(configuration.toDomain(), ip).toLeft()
}

suspend fun updateDnsRecord(
    domain: Domain, ip: IpAddress
) = updateDnsRecord(domain, ip, ProductionApi(domain.apiKey))

internal suspend fun updateDnsRecord(
    domain: Domain, ip: IpAddress, api: Api
): DnsRecordUpdateResult {
    return runCatching {
        val zone = getZone(api, domain.rootDomainName).leftOr { return it }
        val dnsRecord = getDnsRecord(zone, domain.targetDomainName).leftOr { return it }
        val dnsRecordUpdate = createDnsRecordUpdate(dnsRecord, ip, domain.timeToLive)
        updateDnsRecord(api, zone, dnsRecord, dnsRecordUpdate)
    }.getOrElse(::UnknownError)
}

internal suspend fun getZone(api: Api, rootDomainName: String): Either<Zone, DnsRecordUpdateResult> {
    val zones = when (val apiResult = api.getZones()) {
        is ApiResult.Exception -> return UnknownError(apiResult.throwable).toRight()
        is ApiResult.Unauthorized -> return IonosUnauthorized.toRight()
        is ApiResult.Success -> apiResult.value
    }

    val zoneDescriptor = zones.firstOrNull { zone -> zone.name == rootDomainName }
        ?: return MissingRootDomain.toRight()

    return when (val apiResult = api.getZone(zoneDescriptor.id)) {
        is ApiResult.Exception -> UnknownError(apiResult.throwable).toRight()
        is ApiResult.Unauthorized -> IonosUnauthorized.toRight()
        is ApiResult.Success -> return apiResult.value.toLeft()
    }
}

internal fun getDnsRecord(
    zone: Zone, targetDomainName: String
): Either<DnsRecord, DnsRecordUpdateResult> {
    return zone.records
        .firstOrNull { record -> record.name == targetDomainName }?.toLeft()
        ?: MissingTargetDomain.toRight()
}

internal fun createDnsRecordUpdate(
    dnsRecord: DnsRecord,
    ip: IpAddress,
    timeToLive: Duration
): DnsRecordUpdate {
    return dnsRecord.toDnsRecordPatch()
        .copy(content = ip)
        .copy(ttl = timeToLive.inSeconds.roundToInt())
}

internal suspend fun updateDnsRecord(
    api: Api, zone: Zone, record: DnsRecord, update: DnsRecordUpdate
): DnsRecordUpdateResult {
    return when (val apiResult = api.putRecord(zoneId = zone.id, recordId = record.id, update)) {
        is ApiResult.Exception -> UnknownError(apiResult.throwable)
        is ApiResult.Unauthorized -> IonosUnauthorized
        is ApiResult.Success -> Success
    }
}
