package io.sellmair.ionos.dyndns.cli
import kotlinx.coroutines.runBlocking

fun main(args: Array<String>) {
    runBlocking {
        updateDnsRecord(parseDomainConfiguration(args))
    }
}
