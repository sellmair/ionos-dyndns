@file:Suppress("UNUSED_VARIABLE")

plugins {
    application
    kotlin("multiplatform")
    kotlin("plugin.serialization")
}

application {
    applicationName = "ionos-dyndns-service"
    mainClass.set("io.sellmair.ionos.dyndns.service.MainKt")
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
            baseName = "ionos-dyndns-cli"
            entryPoint("io.sellmair.ionos.dyndns.service.main")
        }
    }

    commonMain.dependencies {
        implementation(project(":core"))
        implementation("org.jetbrains.kotlinx:kotlinx-cli:0.3")
        implementation("io.ktor:ktor-client-serialization:1.4.3")
        implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.0.1")
    }

    jvmMain.dependencies {
        implementation(kotlin("stdlib-jdk8"))
    }

    commonTest.dependencies {
        // MPP CORE: Why are those not added by default?
        implementation(kotlin("test-common"))
        implementation(kotlin("test-annotations-common"))
    }

    jvmTest.dependencies {
        implementation(kotlin("test-junit"))
    }
}
