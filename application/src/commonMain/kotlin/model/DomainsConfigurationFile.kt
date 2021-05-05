package io.sellmair.ionos.dyndns.model

import io.sellmair.ionos.dyndns.model.DomainsConfigurationFile.FileReadingFailure.*
import io.sellmair.ionos.dyndns.util.*
import io.sellmair.ionos.dyndns.util.Logger.Diagnostic
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.json.Json

data class DomainsConfigurationFile(val file: File) {

    sealed class FileReadingFailure {
        abstract val file: File

        data class FileNotFound(override val file: File) : FileReadingFailure()
        data class FileIsDirectory(override val file: File) : FileReadingFailure()

        data class FileNotDeserializable(
            override val file: File, val fileContent: String, val reason: Throwable?
        ) : FileReadingFailure()

        data class UnknownFailure(override val file: File, val reason: Throwable) : FileReadingFailure()
    }

    fun readDomains(): Either<List<DomainDTO>, FileReadingFailure> {
        if (!file.exists) return FileNotFound(file).toRight()
        if (file.isDirectory) return FileIsDirectory(file).toRight()
        val fileContent = runCatching { file.readText() }.getOrElse { return UnknownFailure(file, it).toRight() }
        return runCatching { Json.decodeFromString(ListSerializer(DomainDTO.serializer()), fileContent) }
            .fold(
                onSuccess = { it.toLeft() },
                onFailure = { FileNotDeserializable(file, fileContent, it).toRight() }
            )
    }

    fun writeDomains(domains: List<DomainDTO>) {
        file.writeText(Json.encodeToString(ListSerializer(DomainDTO.serializer()), domains))
    }

    companion object {
        val default get() = DomainsConfigurationFile(userHome.resolve(".ionos-dyndns/domains.json"))
    }
}

fun DomainsConfigurationFile.FileReadingFailure.toDiagnostic(): Diagnostic {
    val details = file.path + "\n" + when (this) {
        is FileIsDirectory -> "File is a directory"
        is FileNotDeserializable -> "File is not deserializable\n$fileContent"
        is FileNotFound -> "File not found"
        is UnknownFailure -> "Unknown failure: ${reason.message}\n${reason.stackTraceToString()}"
    }

    return Diagnostic(
        title = "Failed reading domain configuration file",
        details = details,
        throwable = when (this) {
            is FileNotDeserializable -> this.reason
            is UnknownFailure -> this.reason
            else -> null
        }
    )
}
