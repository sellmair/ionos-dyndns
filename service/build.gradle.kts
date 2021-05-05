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
            entryPoint("io.sellmair.ionos.dyndns.service.main")
        }
    }

    commonMain.dependencies {
        implementation(project(":core"))
        implementation(libs.kotlinx.cli)
        implementation(libs.ktor.client.serialization)
        implementation(libs.kotlinx.serialization.json)
    }


    commonTest.dependencies {
        implementation(libs.bundles.kotlin.test)
    }
}

/* Configure jvm distribution */
application {
    applicationName = "ionos-dyndns-service"
    mainClass.set("io.sellmair.ionos.dyndns.service.MainKt")
}

val serviceJar = tasks.register("serviceJar") {
    dependsOn("jvmJar")
    val inputFile = buildDir.resolve("libs/service-jvm.jar")
    val outputFile = buildDir.resolve("libs/service.jar")
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
