@file:Suppress("UNUSED_VARIABLE")

plugins {
    application
    kotlin("multiplatform")
    kotlin("plugin.serialization")
}

kotlin {

    /* Create targets */
    val jvm = jvm { withJava() }
    val macos = macosX64("macos")
    val linux = linuxX64("linux")

    /* Setup source set hierarchy */
    val commonMain by sourceSets.getting
    val commonTest by sourceSets.getting
    val nativeMain by sourceSets.creating
    val nativeTest by sourceSets.creating
    val jvmMain by sourceSets.getting
    val jvmTest by sourceSets.getting
    val macosMain by sourceSets.getting
    val linuxMain by sourceSets.getting
    val macosTest by sourceSets.getting
    val linuxTest by sourceSets.getting

    nativeMain.dependsOn(commonMain)
    nativeTest.dependsOn(commonTest)
    macosMain.dependsOn(nativeMain)
    linuxMain.dependsOn(nativeMain)
    macosTest.dependsOn(nativeTest)
    linuxTest.dependsOn(nativeTest)

    /* Configure binaries */
    listOf(linux, macos).forEach { target ->
        target.binaries.executable {
            baseName = "ionos-dyndns"
            entryPoint("io.sellmair.ionos.dyndns.main")
        }
    }

    commonMain.dependencies {
        implementation(libs.kotlinx.cli)
        implementation(libs.bundles.ktor.client.common)
        implementation(libs.ktor.client.serialization)
        implementation(libs.kotlinx.serialization.json)
    }

    commonTest.dependencies {
        implementation(libs.bundles.kotlin.test)
    }

    nativeMain.dependencies {
        implementation(libs.ktor.client.curl)
        implementation(libs.kotlinx.coroutines.core)
    }

    jvmMain.dependencies {
        implementation(libs.ktor.client.okhttp)
    }
}

/* Configure jvm distribution */
application {
    applicationName = "ionos-dyndns"
    mainClass.set("io.sellmair.ionos.dyndns.MainKt")
}

tasks.run.configure {
    standardInput = System.`in`
}

val serviceJar = tasks.register("applicationJar") {
    dependsOn("jvmJar")
    val inputFile = buildDir.resolve("libs/application-jvm.jar")
    val outputFile = buildDir.resolve("libs/application.jar")
    inputs.file(inputFile)
    outputs.file(outputFile)
    doLast {
        inputFile.copyTo(outputFile, overwrite = true)
    }
}

tasks.named("distTar") {
    dependsOn(serviceJar)
}

tasks.named("distZip") {
    dependsOn(serviceJar)
}
