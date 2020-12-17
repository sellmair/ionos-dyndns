package io.sellmair.ionos.dyndns.service

import io.sellmair.ionos.dyndns.cli.DomainConfiguration
import io.sellmair.ionos.dyndns.cli.IpAddress
import io.sellmair.ionos.dyndns.cli.PublicIpProvider

class IpAdressCache(private val ipAdresses: Map<DomainConfiguration, IpAddress>) {

}
