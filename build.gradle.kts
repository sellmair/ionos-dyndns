@file:Suppress("UNUSED_VARIABLE")

import org.gradle.internal.os.OperatingSystem
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension
import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTarget
import org.jetbrains.kotlin.gradle.targets.jvm.tasks.KotlinJvmTest
import org.jetbrains.kotlin.gradle.targets.native.tasks.KotlinNativeHostTest

plugins {
    kotlin("multiplatform") apply false
    kotlin("plugin.serialization") apply false
}

allprojects {
    repositories {
        maven("https://maven.pkg.jetbrains.space/public/p/ktor/eap") {
            mavenContent { includeGroup("io.ktor") }
        }

        maven("https://maven.pkg.jetbrains.space/public/p/kotlinx-coroutines/maven") {
            mavenContent { includeGroup("org.jetbrains.kotlinx") }
        }

        maven("https://maven.pkg.jetbrains.space/kotlin/p/kotlin/dev")
        maven("https://maven.pkg.jetbrains.space/kotlin/p/kotlin/eap")
        mavenCentral()
    }
}

allprojects {
    plugins.withId("kotlin-multiplatform") {
        extensions.getByType<KotlinMultiplatformExtension>().run {
            /* Configure Opt-Ins */
            sourceSets.all {
                languageSettings.optIn("kotlin.RequiresOptIn")
                languageSettings.optIn("kotlin.time.ExperimentalTime")
                languageSettings.optIn("kotlin.ExperimentalUnsignedTypes")
            }

            /* Configure test environment variables */
            val testEnvironment = properties.entries.filter { (key, _) -> key.startsWith("io.sellmair.ionos.test") }
                .map { (key, value) -> key.toUpperCase().replace(".", "_") to value.toString() }

            tasks.withType<KotlinNativeHostTest>().configureEach {
                testEnvironment.forEach { (key, value) -> environment(key, value) }
            }
            tasks.withType<KotlinJvmTest>().configureEach {
                testEnvironment.forEach { (key, value) -> environment(key, value) }
            }

            /* Disable cross compilation */
            afterEvaluate {
                tasks.configureEach {
                    if ("linux" in name.toLowerCase()) {
                        enabled = OperatingSystem.current().isLinux
                    }
                    if ("macos" in name.toLowerCase()) {
                        enabled = OperatingSystem.current().isMacOsX
                    }
                }
            }

            targets.withType<KotlinNativeTarget> {
                binaries.all {
                    binaryOptions["memoryModel"] = "experimental"
                }
            }
        }
    }
}
