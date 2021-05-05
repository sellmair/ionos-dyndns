pluginManagement {
    plugins {
        val sellmair: String? by settings
        val kotlinVersion = if (sellmair == "true") "1.5.255-SNAPSHOT" else "1.5.0"
        kotlin("multiplatform") version kotlinVersion
        kotlin("plugin.serialization") version kotlinVersion
    }

    repositories {
        gradlePluginPortal()
        mavenCentral()
        mavenLocal()
    }
}

include(":core")
include(":service")
