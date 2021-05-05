package io.sellmair.ionos.dyndns

/* TODO
import io.sellmair.ionos.dyndns.model.Domain
import io.sellmair.ionos.dyndns.model.DomainJsonDTO
import io.sellmair.ionos.dyndns.model.parseDomain
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.time.Duration.Companion.seconds

class ParseDomainTest {


    @Test
    fun withFullArgumentName() {
        val domain = parseDomainDTO(
            arrayOf(
                "--apiKey", "abc",
                "--timeToLive", "300s",
                "--rootDomainName", "sellmair.io",
                "--targetDomainName", "abc.sellmair.io"
            )
        )

        assertEquals(
            Domain(
                apiKey = "abc",
                timeToLive = 300.seconds,
                rootDomainName = "sellmair.io",
                targetDomainName = "abc.sellmair.io"
            ),
            domain
        )
    }

    @Test
    fun withShortNames() {
        val domain = parseDomain(
            arrayOf(
                "-d", "sellmair.io",
                "-k", "my api key",
                "-ttl", "1000s",
                "-a", "app1.sellmair.io"
            )
        )

        assertEquals(
            Domain(
                rootDomainName = "sellmair.io",
                apiKey = "my api key",
                timeToLive = 1000.seconds,
                targetDomainName = "app1.sellmair.io"
            ),
            domain
        )
    }

    @Test
    fun withDefaultTtl() {
        val domain = parseDomain(
            arrayOf(
                "-d", "sellmair.io",
                "-k", "key",
                "-a", "app.sellmair.io"
            )
        )

        assertEquals(
            Domain(
                apiKey = "key",
                rootDomainName = "sellmair.io",
                targetDomainName = "app.sellmair.io",
                timeToLive = DomainJsonDTO.defaultTimeToLive
            ),
            domain
        )
    }
    
}


 */