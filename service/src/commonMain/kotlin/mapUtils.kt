package io.sellmair.ionos.dyndns.service

fun <K, V : Any> Map<K, V?>.filterNotNullValues(): Map<K, V> {
    @Suppress("unchecked_cast")
    return this.filter { (_, value) -> value != null } as Map<K, V>
}

internal operator fun <K, V> Map<K, V>.minus(other: Map<K, V>): Map<K, V> {
    // MPP CORE: Maybe get rid of this .toPair?
    return (this.entries - other.entries).toList().map { it.toPair() }.toMap()
}
