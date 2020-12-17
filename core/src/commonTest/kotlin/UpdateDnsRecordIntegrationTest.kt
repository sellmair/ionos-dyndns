import io.sellmair.ionos.dyndns.cli.*
import kotlinx.coroutines.runBlocking
import kotlin.math.roundToInt
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail
import kotlin.time.minutes

class UpdateDnsRecordIntegrationTest {

    private val domain = TestDomain()

    @Test
    fun updateDynDnsTestSubdomain() {
        runBlocking {
            val arguments = DomainConfiguration(
                apiKey = domain.apiKey,
                rootDomainName = domain.rootDomainName,
                targetDomainName = domain.targetDomainName,
                timeToLive = 10.minutes
            )

            updateAndVerifyDnsRecord(
                arguments.copy(
                    ipProvider = PublicIpProvider.fromKnownIp("100.100.100.100"),
                    timeToLive = 100.minutes
                )
            )

            updateAndVerifyDnsRecord(
                arguments.copy(
                    ipProvider = PublicIpProvider.fromKnownIp("200.200.200.200"),
                    timeToLive = 10.minutes
                )
            )
        }
    }

    private suspend fun updateAndVerifyDnsRecord(arguments: DomainConfiguration) {
        updateDnsRecord(arguments)

        val api = ProductionApi(arguments.apiKey)
        val zone = getZone(api, arguments.rootDomainName)
            .leftOr { fail("Failed to get zone for ${arguments.rootDomainName}") }

        val record = zone.records.singleOrNull { record ->
            record.name == arguments.targetDomainName && record.type == "A"
        } ?: fail("Failed to get dns record for ${arguments.targetDomainName}")

        assertEquals(
            arguments.ipProvider(), record.content,
            "Expected correct IP address being set"
        )
        assertEquals(
            arguments.timeToLive.inSeconds.roundToInt(), record.ttl,
            "Expected correct ttl being set"
        )
    }
}
