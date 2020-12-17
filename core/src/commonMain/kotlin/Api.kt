package io.sellmair.ionos.dyndns.cli

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.features.json.*
import io.ktor.client.features.json.serializer.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*

internal sealed class ApiResult<out T> {
    data class Exception(val throwable: Throwable) : ApiResult<Nothing>()
    data class Unauthorized(val message: String) : ApiResult<Nothing>()
    data class Success<T>(val value: T) : ApiResult<T>()

    fun successOrThrow(): T {
        return when (this) {
            is Exception -> throw this.throwable
            is Unauthorized -> throw RuntimeException("Unauthorized. Bad api key?\n$message")
            is Success -> this.value
        }
    }
}

internal interface Api {
    suspend fun getZones(): ApiResult<List<ZoneDescriptor>>
    suspend fun getZone(zoneId: String): ApiResult<Zone>
    suspend fun putRecord(zoneId: String, recordId: String, update: DnsRecordUpdate): ApiResult<Unit>
}

// MPP CORE: Moving this into "ProductionApi" will leak resources
internal val client = HttpClient {
    install(JsonFeature) {
        serializer = KotlinxSerializer(kotlinx.serialization.json.Json {
            ignoreUnknownKeys = true
        })
    }
}

internal data class ProductionApi(
    private val apiKey: String,
    private val baseUrl: String = "https://api.hosting.ionos.com/dns/v1"
) : Api {
    override suspend fun getZones(): ApiResult<List<ZoneDescriptor>> {
        return defaultRequest("zones") {
            method = HttpMethod.Get
        }
    }

    override suspend fun getZone(zoneId: String): ApiResult<Zone> {
        return defaultRequest("zones/$zoneId") {
            method = HttpMethod.Get
        }
    }

    override suspend fun putRecord(zoneId: String, recordId: String, update: DnsRecordUpdate): ApiResult<Unit> {
        return defaultRequest("zones/$zoneId/records/${recordId}") {
            contentType(ContentType.Application.Json)
            method = HttpMethod.Put
            body = update
        }
    }

    // MPP CORE: Cannot see sources for ktor. Just see knm files =(
    private suspend inline fun <reified T> defaultRequest(
        relativePath: String,
        block: HttpRequestBuilder.() -> Unit
    ): ApiResult<T> {
        return try {
            client.request<HttpStatement> {
                url("$baseUrl/$relativePath")
                header("X-API-Key", apiKey)
                block()
            }.execute { response ->
                if (response.status == HttpStatusCode.Unauthorized || response.status == HttpStatusCode.Forbidden) {
                    return@execute ApiResult.Unauthorized(message = response.readText())
                }

                if (response.status == HttpStatusCode.BadRequest) {
                    val text = response.readText()
                    return@execute ApiResult.Exception(Throwable(text))
                }

                if (T::class == Unit::class && response.status == HttpStatusCode.OK) {
                    return@execute ApiResult.Success(Unit as T)
                }
                return@execute runCatching { ApiResult.Success(response.call.receive<T>()) }
                    .getOrElse { ApiResult.Exception(it) }
            }
        } catch (t: Throwable) {
            return ApiResult.Exception(t)
        }
    }
}
