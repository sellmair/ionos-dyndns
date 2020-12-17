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
        implementation("io.ktor:ktor-client-core:1.4.3")
        implementation("io.ktor:ktor-client-serialization:1.4.3")
        implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.0.1")
        implementation("org.jetbrains.kotlinx:kotlinx-cli:0.3")
    }
    nativeMain.dependencies {
        implementation("io.ktor:ktor-client-curl:1.4.3")
        implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.4.2-native-mt")
    }

    jvmMain.dependencies {
        implementation(kotlin("test-junit"))
        implementation("io.ktor:ktor-client-okhttp:1.4.3")
    }

    commonTest.dependencies {
        implementation(kotlin("test-common"))
        implementation(kotlin("test-annotations-common"))
    }
}
