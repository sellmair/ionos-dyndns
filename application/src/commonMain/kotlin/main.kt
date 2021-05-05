package io.sellmair.ionos.dyndns

import io.sellmair.ionos.dyndns.model.parseApplicationArguments
import io.sellmair.ionos.dyndns.runner.ApplicationRunner

fun main(args: Array<String>) {
    val applicationConfiguration = parseApplicationArguments(args)
    val applicationRunner = ApplicationRunner(applicationConfiguration.mode)
    applicationRunner(applicationConfiguration.publicIpProvider, applicationConfiguration.domainsConfigurationFile)
}
