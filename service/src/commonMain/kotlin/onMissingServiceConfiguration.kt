package io.sellmair.ionos.dyndns.service

fun onMissingServiceConfiguration() {
    println(
        """
            Missing configuration file. 
            Please provide a file as "-f" argument
            Or create one in ${defaultConfigurationFile.path}
        """.trimIndent()
    )
}
