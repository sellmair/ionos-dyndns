package io.sellmair.ionos.dyndns.model

import io.sellmair.ionos.dyndns.api.PublicIpProvider
import io.sellmair.ionos.dyndns.api.parseArgumentString
import io.sellmair.ionos.dyndns.model.ApplicationArguments.Mode
import io.sellmair.ionos.dyndns.model.ApplicationArguments.Mode.Daemon.Companion.defaultIpRefreshInterval
import io.sellmair.ionos.dyndns.util.File
import io.sellmair.ionos.dyndns.util.toDuration
import kotlinx.cli.ArgParser
import kotlinx.cli.ArgType
import kotlin.time.Duration
import kotlin.time.Duration.Companion.minutes

data class ApplicationArguments(
    val mode: Mode,
    val publicIpProvider: PublicIpProvider,
    val domainsConfigurationFile: DomainsConfigurationFile
) {
    sealed class Mode {
        object SingleInvocation : Mode()
        data class Daemon(val ipRefreshInterval: Duration) : Mode() {
            companion object {
                val defaultIpRefreshInterval = 5.minutes
            }
        }
    }
}

fun parseApplicationArguments(args: Array<String>): ApplicationArguments {
    val parser = ArgParser("ionos-dyndns")
    val daemonMode by parser.option(ArgType.Boolean, "daemon", "d", "Run ionos-dyndns in damon mode")
    val configurationFilePath by parser.option(ArgType.String, "config", "c", "Path to configuration file")
    val ipRefreshInterval by parser.option(ArgType.String, "ipRefreshInterval", "i", "Duration between refreshing ip")
    val publicIpProvider by parser.option(ArgType.String, "ipProvider", "ip", "Ip Provider")
    parser.parse(args)

    val mode = if (daemonMode == true) Mode.Daemon(
        ipRefreshInterval = ipRefreshInterval?.toDuration() ?: defaultIpRefreshInterval
    ) else Mode.SingleInvocation

    val domainsConfigurationFile = configurationFilePath?.let(::File)?.let(::DomainsConfigurationFile)
        ?: DomainsConfigurationFile.default

    return ApplicationArguments(
        mode = mode,
        publicIpProvider = PublicIpProvider.parseArgumentString(publicIpProvider),
        domainsConfigurationFile = domainsConfigurationFile
    )
}
