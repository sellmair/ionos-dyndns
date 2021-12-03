pluginManagement {
    plugins {
        val kotlinVersion = "1.6.20-dev-5283"
        kotlin("multiplatform") version kotlinVersion
        kotlin("plugin.serialization") version kotlinVersion
    }

    repositories {
        gradlePluginPortal()
        mavenCentral()
        mavenLocal()
        maven("https://maven.pkg.jetbrains.space/kotlin/p/kotlin/dev")
        maven("https://maven.pkg.jetbrains.space/kotlin/p/kotlin/eap")
    }
}

include(":application")
enableFeaturePreview("VERSION_CATALOGS")
