package io.sellmair.ionos.dyndns.service

import io.sellmair.ionos.dyndns.cli.DomainConfigurationDTO
import kotlin.test.Test
import kotlin.test.assertEquals

class ServiceConfigurationDTOSerializationTest {

    @Test
    fun deserializeFromString() {
        val jsonString =
            """
            {
              "ipRefreshInterval": "60s",
              "domains": [
                {
                  "apiKey": "myApiKey",
                  "rootDomainName": "sellmair.io",
                  "targetDomainName": "test.sellmair.io",
                  "timeToLive": "5m",
                  "ipProvider": "amazon"
                }
              ]
            }
            """.trimIndent()

        assertEquals(
            ServiceConfigurationDTO(
                ipRefreshInterval = "60s",
                domains = listOf(
                    DomainConfigurationDTO(
                        apiKey = "myApiKey",
                        rootDomainName = "sellmair.io",
                        targetDomainName = "test.sellmair.io",
                        timeToLive = "5m",
                        ipProvider = "amazon"
                    )
                )
            ), ServiceConfigurationDTO.fromJson(jsonString)
        )
    }

    @Test
    fun serializeAndDeserialize() {
        val configuration = ServiceConfigurationDTO(
            ipRefreshInterval = "16m",
            domains = listOf(
                DomainConfigurationDTO(
                    apiKey = "a",
                    rootDomainName = "b",
                    targetDomainName = "c"
                ),
                DomainConfigurationDTO(
                    apiKey = "d",
                    rootDomainName = "e",
                    targetDomainName = "f",
                    ipProvider = "g",
                    timeToLive = "c"
                ),
            )
        )

        val configurationJson = configuration.toJson()
        assertEquals(configuration, ServiceConfigurationDTO.fromJson(configurationJson))
    }
}
