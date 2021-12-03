package io.sellmair.ionos.dyndns

import io.sellmair.ionos.dyndns.util.*
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json

class ConfigFile(val file: File) {
    companion object {
        val inUserHome by lazy {
            ConfigFile(userHome.resolve(".ionos-dyndns").resolve("config.json"))
        }
    }

    object WritingSuccess

    sealed interface WritingFailure {
        val file: File
    }

    sealed interface ReadingFailure {
        val file: File
    }

    data class NotAFile(override val file: File) : ReadingFailure, WritingFailure

    data class FileNotFound(override val file: File) : ReadingFailure, WritingFailure

    data class UnknownFailure(override val file: File, val reason: Throwable?) : ReadingFailure, WritingFailure

    data class ParsingFailure(
        override val file: File, val fileContent: String, val reason: Throwable
    ) : ReadingFailure


    fun read(): Either<DyndnsDomains, ReadingFailure> {
        if (!file.exists) return FileNotFound(file).toRight()
        if (!file.isFile) return NotAFile(file).toRight()
        return try {
            val fileContent = file.readText()
            try {
                Json.decodeFromString<DyndnsDomains>(fileContent).toLeft()
            } catch (t: Throwable) {
                ParsingFailure(file, fileContent, t).toRight()
            }
        } catch (t: Throwable) {
            UnknownFailure(file, t).toRight()
        }
    }

    fun write(dyndnsBulk: DyndnsDomains): Either<WritingSuccess, WritingFailure> {
        if (file.isDirectory) return NotAFile(file).toRight()
        return try {
            val json = Json { prettyPrint = true }
            file.parent.createDirectory()
            file.writeText(json.encodeToString(DyndnsDomains.serializer(), dyndnsBulk))
            WritingSuccess.toLeft()
        } catch (t: Throwable) {
            UnknownFailure(file, t).toRight()
        }
    }
}

internal fun exit(failure: ConfigFile.WritingFailure): Nothing {
    when (failure) {
        is ConfigFile.NotAFile -> exitNotAFile(failure)
        is ConfigFile.UnknownFailure -> exitUnknownFailure(failure)
        is ConfigFile.FileNotFound -> exitFileNotFound(failure)
    }
}

internal fun exit(failure: ConfigFile.ReadingFailure): Nothing {
    when (failure) {
        is ConfigFile.FileNotFound -> exitFileNotFound(failure)
        is ConfigFile.NotAFile -> exitNotAFile(failure)
        is ConfigFile.UnknownFailure -> exitUnknownFailure(failure)
        is ConfigFile.ParsingFailure -> exitParsingFailure(failure)
    }
}

internal fun exitNotAFile(failure: ConfigFile.NotAFile): Nothing {
    Logger.logFatal(
        "${failure.file} is not a file (isDirectory: ${failure.file.isDirectory}, isFile ${failure.file.isFile}"
    )
}

internal fun exitParsingFailure(failure: ConfigFile.ParsingFailure): Nothing {
    Logger.logFatal(
        """
        |Failed parsing file:
        |file: ${failure.file}
        |
        |content: 
        ${failure.fileContent.prependIndent("|")}
        """.trimMargin()
    )
}

internal fun exitUnknownFailure(failure: ConfigFile.UnknownFailure): Nothing {
    Logger.logFatal(
        """
            |Unknown failure when reading: ${failure.file}
            |${failure.reason?.stackTraceToString()?.prependIndent("|")}
        """.trimMargin()
    )
}

internal fun exitFileNotFound(failure: ConfigFile.FileNotFound): Nothing {
    Logger.logFatal(
        "${failure.file}: File not found"
    )
}
