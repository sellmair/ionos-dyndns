package io.sellmair.ionos.dyndns

import io.sellmair.ionos.dyndns.api.PublicIpProvider
import io.sellmair.ionos.dyndns.model.ApplicationArguments
import io.sellmair.ionos.dyndns.model.DomainsConfigurationFile
import io.sellmair.ionos.dyndns.model.parseApplicationArguments
import io.sellmair.ionos.dyndns.util.File
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.time.Duration.Companion.minutes

class ParseApplicationArgumentsTest {

    @Test
    fun `test all options with full name`() {
        val arguments = parseApplicationArguments(
            "--daemon --config path/to/file/domains.json --ipRefreshInterval 2m --ipProvider ip:20.20.20.20"
                .split(" ").toTypedArray()
        )

        assertEquals(
            ApplicationArguments(
                mode = ApplicationArguments.Mode.Daemon(2.minutes),
                publicIpProvider = PublicIpProvider.fromKnownIp("20.20.20.20"),
                domainsConfigurationFile = DomainsConfigurationFile(File("path/to/file/domains.json"))
            ),
            arguments
        )
    }

    @Test
    fun `test all options with short name`() {
        val arguments = parseApplicationArguments(
            "-d -c path/to/file/domains.json -i 2m -ip ip:20.20.20.20"
                .split(" ").toTypedArray()
        )

        assertEquals(
            ApplicationArguments(
                mode = ApplicationArguments.Mode.Daemon(2.minutes),
                publicIpProvider = PublicIpProvider.fromKnownIp("20.20.20.20"),
                domainsConfigurationFile = DomainsConfigurationFile(File("path/to/file/domains.json"))
            ),
            arguments
        )
    }

    @Test
    fun `test with empty arguments`() {
        val arguments = parseApplicationArguments(emptyArray())
        assertEquals(
            ApplicationArguments(
                mode = ApplicationArguments.Mode.SingleInvocation,
                publicIpProvider = PublicIpProvider.default,
                domainsConfigurationFile = DomainsConfigurationFile.default
            ),
            arguments
        )
    }
}
