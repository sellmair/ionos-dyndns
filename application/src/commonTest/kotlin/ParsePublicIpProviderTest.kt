package io.sellmair.ionos.dyndns

import io.sellmair.ionos.dyndns.api.AmazonPublicIpProvider
import io.sellmair.ionos.dyndns.api.PublicIpProvider
import io.sellmair.ionos.dyndns.api.parseArgumentString
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class ParsePublicIpProviderTest {

    @Test
    fun `test parse amazon`() {
        assertEquals(AmazonPublicIpProvider, PublicIpProvider.parseArgumentString("amazon"))
    }

    @Test
    fun `test parse null argument`() {
        assertEquals(PublicIpProvider.default, PublicIpProvider.parseArgumentString(null))
    }

    @Test
    fun `test parse url`() {
        assertEquals(
            PublicIpProvider.fromUrl("test.getip.com"), PublicIpProvider.parseArgumentString("url:test.getip.com")
        )
    }

    @Test
    fun `test parse ip`() {
        assertEquals(
            PublicIpProvider.fromKnownIp("10.10.10.10"), PublicIpProvider.parseArgumentString("ip:10.10.10.10")
        )
    }

    @Test
    fun `test parse invalid input`() {
        assertFailsWith<IllegalArgumentException> {
            PublicIpProvider.parseArgumentString("invalid:xxx")
        }
    }
}