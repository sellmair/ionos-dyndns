package io.sellmair.ionos.dyndns.api

import io.ktor.client.request.*

typealias IpAddress = String

interface PublicIpProvider {
    suspend operator fun invoke(): IpAddress?

    companion object Factory {
        val default get() = amazon()
        fun amazon(): PublicIpProvider = AmazonPublicIpProvider
        fun fromUrl(url: String): PublicIpProvider = UrlPublicIpProvider(url)
        fun fromKnownIp(ip: String): PublicIpProvider = KnownIpProvider(ip)
    }
}

internal data class KnownIpProvider(private val publicIp: String) : PublicIpProvider {
    override suspend fun invoke(): String = publicIp
}

internal object AmazonPublicIpProvider : PublicIpProvider by UrlPublicIpProvider(
    url = "https://checkip.amazonaws.com/"
) {
    override fun toString(): String {
        return "AmazonPublicIpProvider"
    }
}

internal data class UrlPublicIpProvider(val url: String) : PublicIpProvider {
    override suspend fun invoke(): String? {
        //MPP Core: Allocating new HttpClient().use will create many, many threads over time
        // It still seems like we have a memory leak somewhere :(
        return runCatching {
            return client.get<String> { this.url(this@UrlPublicIpProvider.url) }.trim()
        }.getOrNull()
    }
}

fun PublicIpProvider.Factory.parseArgumentString(argument: String?): PublicIpProvider {
    return when {
        argument == null -> default
        argument == "amazon" -> amazon()
        argument.startsWith("url:") -> fromUrl(argument.removePrefix("url:"))
        argument.startsWith("ip:") -> fromKnownIp(argument.removePrefix("ip:"))
        else -> throw IllegalArgumentException("Unknown Ip Provider value: $argument")
    }
}