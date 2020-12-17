import io.sellmair.ionos.dyndns.cli.environmentVariable

class TestDomain {
    val apiKey = environmentVariable(
        "IO_SELLMAIR_IONOS_TEST_API_KEY"
    )

    val rootDomainName = environmentVariable(
        "IO_SELLMAIR_IONOS_TEST_ROOT_DOMAIN"
    )

    val targetDomainName = environmentVariable(
        "IO_SELLMAIR_IONOS_TEST_A_RECORD_DOMAIN_NAME"
    )
}
