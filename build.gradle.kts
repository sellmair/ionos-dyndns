@file:Suppress("UNUSED_VARIABLE")

import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension
import org.jetbrains.kotlin.gradle.targets.jvm.tasks.KotlinJvmTest
import org.jetbrains.kotlin.gradle.targets.native.tasks.KotlinNativeHostTest

plugins {
    kotlin("multiplatform") apply false
    kotlin("plugin.serialization") apply false
}

allprojects {
    repositories {
        mavenCentral()
        jcenter()
        maven("https://kotlin.bintray.com/kotlinx")
        mavenLocal()
    }
}

allprojects {
    plugins.withId("kotlin-multiplatform") {
        extensions.getByType<KotlinMultiplatformExtension>().run {
            /* Configure Opt-Ins */
            sourceSets.all {
                languageSettings.useExperimentalAnnotation("kotlin.RequiresOptIn")
                languageSettings.useExperimentalAnnotation("kotlin.time.ExperimentalTime")
                languageSettings.useExperimentalAnnotation("kotlin.ExperimentalUnsignedTypes")
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
        }
    }
}
