import io.sellmair.ionos.dyndns.cli.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import kotlin.math.roundToInt
import kotlin.random.Random
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail
import kotlin.time.minutes
import kotlin.time.seconds

class UpdateDnsRecordIntegrationTest {

    private val ipAddressSignallingIdle = "1.1.1.1"


    private val domain = TestDomain()

    private val domainConfiguration = DomainConfiguration(
        apiKey = domain.apiKey,
        rootDomainName = domain.rootDomainName,
        targetDomainName = domain.targetDomainName,
        timeToLive = 10.minutes
    )

    private val api = ProductionApi(domainConfiguration.apiKey)


    @Test
    fun updateDynDnsTestSubdomain() {
        runBlocking {
            withTestingDomain {
                updateAndVerifyDnsRecord(
                    domainConfiguration.copy(
                        ipProvider = PublicIpProvider.fromKnownIp("100.100.100.100"),
                        timeToLive = 100.minutes
                    )
                )
                updateAndVerifyDnsRecord(
                    domainConfiguration.copy(
                        ipProvider = PublicIpProvider.fromKnownIp("200.200.200.200"),
                        timeToLive = 10.minutes
                    )
                )
            }
        }
    }

    private suspend fun updateAndVerifyDnsRecord(arguments: DomainConfiguration) {
        updateDnsRecord(arguments)

        val record = getDnsRecord(arguments)

        assertEquals(
            arguments.ipProvider(), record.content,
            "Expected correct IP address being set"
        )
        assertEquals(
            arguments.timeToLive.inSeconds.roundToInt(), record.ttl,
            "Expected correct ttl being set"
        )
    }

    private suspend fun aquireDnsRecordForTesting() {
        getDnsRecord(domainConfiguration)
        try {
            withTimeout(10.minutes) {
                val randomIpForThisProcess = randomIp()
                while (true) {
                    if (getDnsRecord(domainConfiguration).content == ipAddressSignallingIdle) {
                        updateDnsRecord(api, domain = domainConfiguration.toDomain(), ip = randomIpForThisProcess)
                        println("Tried to aquire dns record. Waiting 10 seconds...")
                        delay(10.seconds)
                        if (getDnsRecord(domainConfiguration).content == randomIpForThisProcess) {
                            println("Dns record aquired for testing!")
                            break
                        }
                    }
                    println("Waiting for dns record to get free...")
                    delay(30.seconds)
                }
            }
        } catch (t: Throwable) {
            println("Failed to gently aquire dns record for testing.")
            println("Continuing anyways 🤷‍♂️")
        }
    }

    private suspend fun releaseDnsRecordForTesting() {
        updateDnsRecord(api, domainConfiguration.toDomain(), ipAddressSignallingIdle)
    }

    private suspend inline fun withTestingDomain(action: () -> Unit) {
        aquireDnsRecordForTesting()
        try {
            action()
        } finally {
            releaseDnsRecordForTesting()
        }
    }

    private suspend fun getDnsRecord(arguments: DomainConfiguration): DnsRecord {
        val zone = getZone(api, arguments.rootDomainName)
            .leftOr { fail("Failed to get zone for ${arguments.rootDomainName}") }

        return zone.records.singleOrNull { record ->
            record.name == arguments.targetDomainName && record.type == "A"
        } ?: fail("Failed to get dns record for ${arguments.targetDomainName}")
    }

    private fun randomIp(): String {
        fun randomPart() = Random.nextInt(2, 99)
        return "${randomPart()}.${randomPart()}.${randomPart()}.${randomPart()}"
    }

}
