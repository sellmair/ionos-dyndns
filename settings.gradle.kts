pluginManagement {
    plugins {
        kotlin("multiplatform") version "1.4.21"
        kotlin("plugin.serialization") version "1.4.21"
    }

    repositories {
        gradlePluginPortal()
        mavenCentral()
        mavenLocal()
    }
}

include(":core")
include(":service")
