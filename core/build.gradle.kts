@file:Suppress("UNUSED_VARIABLE")

plugins {
    kotlin("multiplatform")
    kotlin("plugin.serialization")
}

kotlin {

    /* Create targets */
    val jvm = jvm()
    val macos = macosX64("macos")
    val linux = linuxX64("linux")

    /* Setup source set hierarchy */
    val commonMain by sourceSets.getting
    val commonTest by sourceSets.getting
    val nativeMain by sourceSets.creating
    val jvmMain by sourceSets.getting
    val macosMain by sourceSets.getting
    val linuxMain by sourceSets.getting


    nativeMain.dependsOn(commonMain)
    macosMain.dependsOn(nativeMain)
    linuxMain.dependsOn(nativeMain)

    /* Configure binaries */
    listOf(linux, macos).forEach { target ->
        target.binaries.executable {
            baseName = "ionos-dyndns-cli"
            entryPoint("io.sellmair.ionos.dyndns.cli.main")
        }
    }

    /* Configure dependencies */
    commonMain.dependencies {
        implementation(libs.bundles.ktor.client.common)
        implementation(libs.kotlinx.serialization.json)
        implementation(libs.kotlinx.cli)
    }
    nativeMain.dependencies {
        implementation(libs.ktor.client.curl)
        implementation(libs.kotlinx.coroutines.core)
    }

    jvmMain.dependencies {
        implementation(libs.ktor.client.okhttp)
    }

    commonTest.dependencies {
        implementation(libs.bundles.kotlin.test)
    }
}
