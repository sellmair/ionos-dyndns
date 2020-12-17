import io.sellmair.ionos.dyndns.cli.*
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.time.seconds

class ParseDomainConfigurationTest {

    @Test
    fun withFullArgumentName() {
        val arguments = parseDomainConfiguration(
            arrayOf(
                "--apiKey", "abc",
                "--timeToLive", "300s",
                "--rootDomainName", "sellmair.io",
                "--targetDomainName", "abc.sellmair.io"
            )
        )

        assertEquals(
            DomainConfiguration(
                apiKey = "abc",
                timeToLive = 300.seconds,
                rootDomainName = "sellmair.io",
                targetDomainName = "abc.sellmair.io"
            ),
            arguments
        )
    }

    @Test
    fun withShortNames() {
        val arguments = parseDomainConfiguration(
            arrayOf(
                "-d", "sellmair.io",
                "-k", "my api key",
                "-ttl", "1000s",
                "-a", "app1.sellmair.io"
            )
        )

        assertEquals(
            DomainConfiguration(
                rootDomainName = "sellmair.io",
                apiKey = "my api key",
                timeToLive = 1000.seconds,
                targetDomainName = "app1.sellmair.io"
            ),
            arguments
        )
    }

    @Test
    fun withDefaultTtl() {
        val arguments = parseDomainConfiguration(
            arrayOf(
                "-d", "sellmair.io",
                "-k", "key",
                "-a", "app.sellmair.io"
            )
        )

        assertEquals(
            DomainConfiguration(
                apiKey = "key",
                rootDomainName = "sellmair.io",
                targetDomainName = "app.sellmair.io"
            ),
            arguments
        )
    }

    @Test
    fun withAmazonIpProvider() {
        val arguments = parseDomainConfiguration(
            arrayOf(
                "-d", "sellmair.io",
                "-k", "key",
                "-a", "app.sellmair.io",
                "--ipProvider", "amazon"
            )
        )

        assertEquals(AmazonPublicIpProvider, arguments.ipProvider)
    }

    @Test
    fun withCustomUrlIpProvider() {
        val arguments = parseDomainConfiguration(
            arrayOf(
                "-d", "sellmair.io",
                "-k", "key",
                "-a", "app.sellmair.io",
                "-ip", "url:https://my.ip-service.com"
            )
        )

        assertEquals(UrlPublicIpProvider("https://my.ip-service.com"), arguments.ipProvider)
    }

    @Test
    fun withKnownIpProvider() {
        val arguments = parseDomainConfiguration(
            arrayOf(
                "-d", "sellmair.io",
                "-k", "key",
                "-a", "app.sellmair.io",
                "-ip", "ip:100.100.100.100"
            )
        )

        assertEquals(KnownIpProvider("100.100.100.100"), arguments.ipProvider)
    }
}
