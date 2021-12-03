@file:JvmName("FilesJvm")
// MPP CORE: Package should be automatically added
//  when new file in "module", but common main already exits
package io.sellmair.ionos.dyndns.util

import java.io.File as JavaFile

val File.java get() = JavaFile(path)

val JavaFile.common get() = File(path)

actual val File.exists: Boolean get() = java.exists()

actual val File.isFile: Boolean get() = java.isFile

actual val File.isDirectory: Boolean get() = java.isDirectory

actual fun File.readText(): String = java.readText()

actual fun File.writeText(text: String) = java.writeText(text)

actual fun File.createDirectory(): Boolean = java.mkdirs()

actual fun File.delete(): Boolean = java.delete()