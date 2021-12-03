package io.sellmair.ionos.dyndns

import io.sellmair.ionos.dyndns.api.Api
import io.sellmair.ionos.dyndns.api.ApiResult
import io.sellmair.ionos.dyndns.model.DnsRecordUpdateDTO
import io.sellmair.ionos.dyndns.model.Domain
import io.sellmair.ionos.dyndns.model.ZoneDTO
import io.sellmair.ionos.dyndns.model.ZoneDescriptorDTO
import io.sellmair.ionos.dyndns.update.DnsRecordUpdateResult
import io.sellmair.ionos.dyndns.update.updateDnsRecord
import kotlinx.coroutines.runBlocking
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.time.Duration.Companion.minutes

class UpdateDnsRecordTest {

    private val domain = Domain(
        apiKey = "",
        rootDomainName = "sellmair.io",
        targetDomainName = "test.sellmair.io",
        timeToLive = 1.minutes,
    )

    private val ip = "10.0.0.1"

    @Test
    fun unauthorizedApiResult() {
        val api = object : Api {
            override suspend fun getZones(): ApiResult<List<ZoneDescriptorDTO>> = ApiResult.Unauthorized("")
            override suspend fun getZone(zoneId: String): ApiResult<ZoneDTO> = ApiResult.Unauthorized("")
            override suspend fun putRecord(
                zoneId: String, recordId: String, update: DnsRecordUpdateDTO
            ): ApiResult<Unit> = ApiResult.Unauthorized("")
        }

        runBlocking {
            val result = updateDnsRecord(api, domain, ip)
            assertEquals(DnsRecordUpdateResult.IonosUnauthorized, result)
        }
    }

    @Test
    fun missingZone() {
        val api = object : Api {
            override suspend fun getZones(): ApiResult<List<ZoneDescriptorDTO>> = ApiResult.Success(emptyList())
            override suspend fun getZone(zoneId: String): ApiResult<ZoneDTO> = throw NotImplementedError()
            override suspend fun putRecord(
                zoneId: String, recordId: String, update: DnsRecordUpdateDTO
            ): ApiResult<Unit> {
                throw NotImplementedError()
            }
        }

        runBlocking {
            val result = updateDnsRecord(api, domain, ip)
            assertEquals(DnsRecordUpdateResult.MissingRootDomain, result)
        }
    }

    @Test
    fun missingDnsRecord() {
        val zone = ZoneDTO(
            name = "sellmair.io",
            id = "zone-id",
            records = emptyList()
        )

        val zoneDescriptor = ZoneDescriptorDTO(
            name = zone.name,
            id = zone.id,
            type = "A"
        )

        val api = object : Api {
            override suspend fun getZones(): ApiResult<List<ZoneDescriptorDTO>> {
                return ApiResult.Success(listOf(zoneDescriptor))
            }

            override suspend fun getZone(zoneId: String): ApiResult<ZoneDTO> {
                return if (zoneId == zone.id) {
                    ApiResult.Success(zone)
                } else {
                    ApiResult.Exception(Throwable("Requested wrong id"))
                }
            }

            override suspend fun putRecord(
                zoneId: String, recordId: String, update: DnsRecordUpdateDTO
            ): ApiResult<Unit> {
                throw NotImplementedError()
            }
        }

        runBlocking {
            val result = updateDnsRecord(api, domain, ip)
            assertEquals(DnsRecordUpdateResult.MissingTargetDomain, result)
        }
    }
}
