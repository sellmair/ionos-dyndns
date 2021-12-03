package io.sellmair.ionos.dyndns

import io.sellmair.ionos.dyndns.api.PublicIpProvider
import kotlinx.coroutines.runBlocking
import kotlin.test.Test
import kotlin.test.assertNotNull

class AmazonPublicIpProviderIntegrationTest {

    @Test
    fun executeRequest() {
        runBlocking {
            assertNotNull(PublicIpProvider.amazon().invoke())
        }
    }
}
