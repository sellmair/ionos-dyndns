package io.sellmair.ionos.dyndns.service

import kotlinx.cli.ArgParser
import kotlinx.cli.ArgType


internal expect val userHome: File

internal val defaultConfigurationFile: File get() = userHome.resolve(".ionos-dyndns/config.json")

fun readServiceConfigurationOrNull(args: Array<String>): ServiceConfiguration? {
    val parser = ArgParser("ionos-dyndns-service")
    val configurationFilePath by parser.option(ArgType.String, "configuration", "c", "Path to configuration file")
    parser.parse(args)

    /* Precondition: Check if user defined file really exists and is file */
    configurationFilePath?.let { filePath ->
        check(File(filePath).isFile) { "$filePath is not a file" }
    }

    val file = configurationFilePath?.toFile() ?: defaultConfigurationFile
    check(!file.isDirectory) { "${file.path} is directory" }
    if (!file.exists) return null

    return ServiceConfigurationDTO.fromJson(file.readText()).toServiceConfiguration()
}
