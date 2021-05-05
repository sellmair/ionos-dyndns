package io.sellmair.ionos.dyndns

import io.sellmair.ionos.dyndns.api.IpAddress
import io.sellmair.ionos.dyndns.api.ProductionApi
import io.sellmair.ionos.dyndns.model.DnsRecordDTO
import io.sellmair.ionos.dyndns.model.Domain
import io.sellmair.ionos.dyndns.update.*
import io.sellmair.ionos.dyndns.util.Logger.logError
import io.sellmair.ionos.dyndns.util.Logger.logInfo
import io.sellmair.ionos.dyndns.util.leftOr
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import kotlin.math.roundToInt
import kotlin.random.Random
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds
import kotlin.time.DurationUnit

class UpdateDnsRecordIntegrationTest {

    private val ipAddressSignallingIdle = "1.1.1.1"

    private val environment = TestDomainEnvironment()

    private val domain = Domain(
        apiKey = environment.apiKey,
        rootDomainName = environment.rootDomainName,
        targetDomainName = environment.targetDomainName,
        timeToLive = 10.minutes
    )

    private val api = ProductionApi(domain.apiKey)

    @Test
    fun updateDynDnsTestSubdomain() {
        runBlocking {
            withTestingDomain {
                updateAndVerifyDnsRecord(
                    domain = domain.copy(timeToLive = 100.minutes),
                    ipAddress = "100.100.100.100"
                )
                updateAndVerifyDnsRecord(
                    domain = domain.copy(timeToLive = 10.minutes),
                    ipAddress = "200.200.200.200"
                )
            }
        }
    }

    private suspend fun updateAndVerifyDnsRecord(
        domain: Domain, ipAddress: IpAddress
    ) {
        updateDnsRecord(domain, ipAddress)

        val record = getDnsRecord(domain)

        assertEquals(
            ipAddress, record.content,
            "Expected correct IP address being set"
        )
        assertEquals(
            domain.timeToLive.toDouble(DurationUnit.SECONDS).roundToInt(), record.ttl,
            "Expected correct ttl being set"
        )
    }

    private suspend fun acquireDnsRecordForTesting() {
        getDnsRecord(domain)
        try {
            withTimeout(10.minutes) {
                val randomIpForThisProcess = randomIp()
                while (true) {
                    if (getDnsRecord(domain).content == ipAddressSignallingIdle) {
                        val updateDnsRecordResult = updateDnsRecord(api, domain = domain, ip = randomIpForThisProcess)
                        when (updateDnsRecordResult) {
                            is DnsRecordUpdateFailure -> logError(updateDnsRecordResult.toDiagnostic(domain))
                            is DnsRecordUpdateResult.Success -> logInfo("Dns lock sent")
                        }
                        logInfo("Tried to acquire dns record. Waiting 10 seconds...")
                        delay(10.seconds)

                        val reQueriedDnsRecord = getDnsRecord(domain)
                        if (reQueriedDnsRecord.content == randomIpForThisProcess) {
                            logInfo("Dns record acquired for testing!")
                            break
                        }
                    }
                    logInfo("Waiting for dns record to get free...")
                    delay(30.seconds)
                }
            }
        } catch (t: Throwable) {
            logError("Failed to gently acquire dns record for testing.")
            logInfo("Continuing anyways 🤷‍♂️")
        }
    }

    private suspend fun releaseDnsRecordForTesting() {
        updateDnsRecord(api, domain, ipAddressSignallingIdle)
    }

    private suspend inline fun withTestingDomain(action: () -> Unit) {
        acquireDnsRecordForTesting()
        try {
            action()
        } finally {
            releaseDnsRecordForTesting()
        }
    }

    private suspend fun getDnsRecord(domain: Domain): DnsRecordDTO {
        val zone = getZone(api, domain.rootDomainName)
            .leftOr {
                logError(it.toDiagnostic(domain))
                fail("Failed to get zone for ${domain.rootDomainName}")
            }

        return zone.records.singleOrNull { record ->
            record.name == domain.targetDomainName && record.type == "A"
        } ?: fail("Failed to get dns record for ${domain.targetDomainName}")
    }

    private fun randomIp(): String {
        fun randomPart() = Random.nextInt(2, 99)
        return "${randomPart()}.${randomPart()}.${randomPart()}.${randomPart()}"
    }
}
