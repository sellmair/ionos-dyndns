package io.sellmair.ionos.dyndns.update

import io.sellmair.ionos.dyndns.api.Api
import io.sellmair.ionos.dyndns.api.ApiResult
import io.sellmair.ionos.dyndns.api.IpAddress
import io.sellmair.ionos.dyndns.api.ProductionApi
import io.sellmair.ionos.dyndns.model.*
import io.sellmair.ionos.dyndns.update.DnsRecordUpdateResult.*
import io.sellmair.ionos.dyndns.util.Either
import io.sellmair.ionos.dyndns.util.leftOr
import io.sellmair.ionos.dyndns.util.toLeft
import io.sellmair.ionos.dyndns.util.toRight
import kotlin.math.roundToInt
import kotlin.time.DurationUnit.SECONDS

suspend fun updateDnsRecord(
    domain: Domain, ip: IpAddress
) = updateDnsRecord(ProductionApi(domain.apiKey), domain, ip)

internal suspend fun updateDnsRecord(
    api: Api, domain: Domain, ip: IpAddress
): DnsRecordUpdateResult {
    return runCatching {
        val zone = getZone(api, domain.rootDomainName).leftOr { return it }
        val dnsRecord = getDnsRecord(zone, domain.targetDomainName).leftOr { return it }
        val dnsRecordUpdate = dnsRecord.toDnsRecordUpdateDTO()
            .copy(content = ip)
            .copy(ttl = domain.timeToLive.toDouble(SECONDS).roundToInt())
        updateDnsRecord(api, zone, dnsRecord, dnsRecordUpdate)
    }.getOrElse(::UnknownError)
}

internal suspend fun getZone(api: Api, rootDomainName: String): Either<ZoneDTO, DnsRecordUpdateFailure> {
    val zones = when (val apiResult = api.getZones()) {
        is ApiResult.Exception -> return UnknownError(apiResult.throwable).toRight()
        is ApiResult.Unauthorized -> return IonosUnauthorized.toRight()
        is ApiResult.UnknownFailure -> return UnknownError(Throwable(apiResult.message)).toRight()
        is ApiResult.Success -> apiResult.value
    }

    val zoneDescriptor = zones.firstOrNull { zone -> zone.name == rootDomainName }
        ?: return MissingRootDomain.toRight()

    return when (val apiResult = api.getZone(zoneDescriptor.id)) {
        is ApiResult.Exception -> UnknownError(apiResult.throwable).toRight()
        is ApiResult.Unauthorized -> IonosUnauthorized.toRight()
        is ApiResult.UnknownFailure -> UnknownError(Throwable(apiResult.message)).toRight()
        is ApiResult.Success -> return apiResult.value.toLeft()
    }
}

internal fun getDnsRecord(
    zone: ZoneDTO, targetDomainName: String
): Either<DnsRecordDTO, DnsRecordUpdateResult> {
    return zone.records
        .firstOrNull { record -> record.name == targetDomainName }?.toLeft()
        ?: MissingTargetDomain.toRight()
}


internal suspend fun updateDnsRecord(
    api: Api, zone: ZoneDTO, record: DnsRecordDTO, update: DnsRecordUpdateDTO
): DnsRecordUpdateResult {
    return when (val apiResult = api.putRecord(zoneId = zone.id, recordId = record.id, update)) {
        is ApiResult.Exception -> UnknownError(apiResult.throwable)
        is ApiResult.Unauthorized -> IonosUnauthorized
        is ApiResult.UnknownFailure -> UnknownError(Throwable(apiResult.message))
        is ApiResult.Success -> Success
    }
}
