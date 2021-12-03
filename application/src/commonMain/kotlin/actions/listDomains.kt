package io.sellmair.ionos.dyndns.actions

import io.sellmair.ionos.dyndns.ConfigFile
import io.sellmair.ionos.dyndns.exit
import io.sellmair.ionos.dyndns.util.leftOr

fun listDomains() {
    val configFile = ConfigFile.inUserHome
    val currentDyndnsDomains = configFile.read().leftOr(::exit)
    println(
        "ionos-dyndns contains ${currentDyndnsDomains.domains.size} domains " +
            "(Description: ${currentDyndnsDomains.description})"
    )
    currentDyndnsDomains.domains.forEach { domain ->
        println(domain)
    }
}
