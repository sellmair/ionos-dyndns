package io.sellmair.ionos.dyndns.api

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.features.json.*
import io.ktor.client.features.json.serializer.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.sellmair.ionos.dyndns.util.Logger.logFatal
import kotlinx.coroutines.runBlocking

internal sealed class ApiResult<out T> {
    sealed interface Failure
    data class Exception(val throwable: Throwable) : ApiResult<Nothing>(), Failure
    data class Unauthorized(val message: String) : ApiResult<Nothing>(), Failure
    data class UnknownFailure(val message: String) : ApiResult<Nothing>(), Failure
    data class Success<T>(val value: T) : ApiResult<T>()

    fun successOrThrow(): T {
        return when (this) {
            is Success -> this.value
            is Failure -> exit(this)
        }
    }
}

internal interface Api {
    suspend fun createDyndnsBulk(dyndnsBulkDTO: DyndnsBulkDTO): ApiResult<CreatedDyndnsBulkDTO>
    suspend fun updateDyndnsBulk(bulkId: String, dyndnsBulkDTO: DyndnsBulkDTO): ApiResult<Unit>
    suspend fun deleteDyndnsBulk(bulkId: String): ApiResult<Unit>
    fun close()
}

internal fun <T> withApi(apiKey: String, action: suspend Api.() -> T): T {
    val api = ProductionApi(apiKey)
    return try {
        runBlocking { api.action() }
    } finally {
        api.close()
    }
}

private data class ProductionApi(
    private val apiKey: String,
    private val baseUrl: String = "https://api.hosting.ionos.com/dns/v1",
) : Api {

    private val client = HttpClient {
        install(JsonFeature) {
            serializer = KotlinxSerializer(kotlinx.serialization.json.Json {
                ignoreUnknownKeys = true
            })
        }
        expectSuccess = false
    }

    override suspend fun createDyndnsBulk(dyndnsBulkDTO: DyndnsBulkDTO): ApiResult<CreatedDyndnsBulkDTO> {
        return defaultRequest("dyndns") {
            contentType(ContentType.Application.Json)
            method = HttpMethod.Post
            body = dyndnsBulkDTO
        }
    }

    override suspend fun updateDyndnsBulk(bulkId: String, dyndnsBulkDTO: DyndnsBulkDTO): ApiResult<Unit> {
        return defaultRequest("dyndns/$bulkId") {
            contentType(ContentType.Application.Json)
            method = HttpMethod.Put
            body = dyndnsBulkDTO
        }
    }

    override suspend fun deleteDyndnsBulk(bulkId: String): ApiResult<Unit> {
        return defaultRequest("dyndns/$bulkId") {
            contentType(ContentType.Application.Json)
            method = HttpMethod.Delete
        }
    }

    override fun close() {
        client.close()
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

                if (response.status == HttpStatusCode.NotFound) {
                    val text = response.readText()
                    return@execute ApiResult.UnknownFailure(text)
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

internal fun exit(failure: ApiResult.Failure): Nothing {
    when (failure) {
        is ApiResult.Exception -> logFatal("Unknown Error", failure.throwable)
        is ApiResult.Unauthorized -> logFatal("Unauthorized (bad API key?): ${failure.message}")
        is ApiResult.UnknownFailure -> logFatal("Unknown Error")
    }
}
