package io.sellmair.ionos.dyndns

import io.sellmair.ionos.dyndns.actions.addDomain
import io.sellmair.ionos.dyndns.actions.listDomains
import io.sellmair.ionos.dyndns.actions.refreshDomainsDaemon
import io.sellmair.ionos.dyndns.actions.refreshDomainsOrExit
import io.sellmair.ionos.dyndns.util.toDuration
import kotlinx.cli.ArgParser
import kotlinx.cli.ArgType
import kotlinx.cli.default
import kotlin.time.Duration
import kotlin.time.Duration.Companion.minutes

fun main(args: Array<String>) {
    when (val command = parseCliCommand(args)) {
        is CliCommand.ListDomains -> listDomains()
        is CliCommand.AddDomain -> addDomain(command.domain)
        is CliCommand.RemoveDomain -> TODO()
        is CliCommand.RunRefresh -> refreshDomainsOrExit()
        is CliCommand.RunRefreshDaemon -> refreshDomainsDaemon(command.interval)
    }
}

sealed class CliCommand {
    object RunRefresh : CliCommand()
    data class RunRefreshDaemon(val interval: Duration) : CliCommand()

    object ListDomains : CliCommand()
    data class AddDomain(val domain: String) : CliCommand()
    data class RemoveDomain(val domain: String) : CliCommand()
}

fun parseCliCommand(args: Array<String>): CliCommand {
    val parser = ArgParser("ionos-dyndns")

    val listDomains by parser.option(
        type = ArgType.Boolean,
        fullName = "list-domains",
        shortName = "ls",
        description = "List all currently registered subdomains"
    ).default(false)

    val addDomain by parser.option(
        type = ArgType.String,
        fullName = "add-domain",
        shortName = "a",
        description = "Add (sub) domain to dyndns update"
    )

    val removeDomain by parser.option(
        type = ArgType.String,
        fullName = "remove-domain",
        shortName = "rm",
        description = "Remove (sub) domain from dyndns updates"
    )

    val daemonMode by parser.option(
        type = ArgType.Boolean,
        fullName = "daemonMode",
        shortName = "d",
        description = "Keep dyndns service alive (default: true)"
    ).default(true)

    val refreshInterval by parser.option(
        type = DurationArgType,
        fullName = "refreshInterval",
        shortName = "i",
        description = "Time between refreshing (default: 10m)"
    ).default(10.minutes)
    parser.parse(args)

    if (listDomains) {
        return CliCommand.ListDomains
    }

    addDomain?.let { domain ->
        return CliCommand.AddDomain(domain)
    }

    removeDomain?.let { domain ->
        return CliCommand.RemoveDomain(domain)
    }

    return if (daemonMode) CliCommand.RunRefreshDaemon(refreshInterval)
    else CliCommand.RunRefresh
}

object DurationArgType : ArgType<Duration>(true) {
    override fun convert(value: kotlin.String, name: kotlin.String): Duration {
        return value.toDuration()
    }

    override val description: kotlin.String
        get() = "{ Value should be formatted like 12s, 1m, 0.5h, 1d }"
}
