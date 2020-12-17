import io.sellmair.ionos.dyndns.cli.*
import kotlinx.coroutines.runBlocking
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.time.minutes

class UpdateDnsRecordTest {

    private val domain = Domain(
        apiKey = "",
        rootDomainName = "sellmair.io",
        targetDomainName = "test.sellmair.io",
        timeToLive = 1.minutes,
    )

    private val ip = "10.0.0.1"

    @Test
    fun failingIpProvider() {
        val failingIpProvider = object : PublicIpProvider {
            override suspend fun invoke(): IpAddress? = null
        }

        runBlocking {
            val result = updateDnsRecord(domain.toDomainConfiguration(failingIpProvider))
            assertEquals(FailedToObtainIpAdress, result.rightOrNull)
        }
    }

    @Test
    fun unauthorizedApiResult() {
        val api = object : Api {
            override suspend fun getZones(): ApiResult<List<ZoneDescriptor>> = ApiResult.Unauthorized("")
            override suspend fun getZone(zoneId: String): ApiResult<Zone> = ApiResult.Unauthorized("")
            override suspend fun putRecord(
                zoneId: String, recordId: String, update: DnsRecordUpdate
            ): ApiResult<Unit> = ApiResult.Unauthorized("")
        }

        runBlocking {
            val result = updateDnsRecord(domain, ip, api)
            assertEquals(DnsRecordUpdateResult.IonosUnauthorized, result)
        }
    }

    @Test
    fun missingZone() {
        val api = object : Api {
            override suspend fun getZones(): ApiResult<List<ZoneDescriptor>> = ApiResult.Success(emptyList())
            override suspend fun getZone(zoneId: String): ApiResult<Zone> = throw NotImplementedError()
            override suspend fun putRecord(
                zoneId: String, recordId: String, update: DnsRecordUpdate
            ): ApiResult<Unit> {
                throw NotImplementedError()
            }
        }

        runBlocking {
            val result = updateDnsRecord(domain, ip, api)
            assertEquals(DnsRecordUpdateResult.MissingRootDomain, result)
        }
    }

    @Test
    fun missingDnsRecord() {
        val zone = Zone(
            name = "sellmair.io",
            id = "zone-id",
            records = emptyList()
        )

        val zoneDescriptor = ZoneDescriptor(
            name = zone.name,
            id = zone.id,
            type = "A"
        )

        val api = object : Api {
            override suspend fun getZones(): ApiResult<List<ZoneDescriptor>> {
                return ApiResult.Success(listOf(zoneDescriptor))
            }

            override suspend fun getZone(zoneId: String): ApiResult<Zone> {
                return if (zoneId == zone.id) {
                    ApiResult.Success(zone)
                } else {
                    ApiResult.Exception(Throwable("Requested wrong id"))
                }
            }

            override suspend fun putRecord(
                zoneId: String, recordId: String, update: DnsRecordUpdate
            ): ApiResult<Unit> {
                throw NotImplementedError()
            }
        }

        runBlocking {
            val result = updateDnsRecord(domain, ip, api)
            assertEquals(DnsRecordUpdateResult.MissingTargetDomain, result)
        }
    }
}
