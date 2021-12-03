package io.sellmair.ionos.dyndns.util

data class File(val path: String) {
    override fun toString(): String = path

    companion object
}

fun String.toFile(): File = File(this)

val File.Companion.separator: String get() = "/"

fun File.resolve(path: String): File = File("${this.path}${File.separator}$path")

val File.parent get() = File(path.split(File.separator).dropLast(1).joinToString(File.separator))

expect val File.exists: Boolean

expect val File.isFile: Boolean

expect val File.isDirectory: Boolean

expect fun File.readText(): String

expect fun File.writeText(text: String)

expect fun File.createDirectory(): Boolean

expect fun File.delete(): Boolean
