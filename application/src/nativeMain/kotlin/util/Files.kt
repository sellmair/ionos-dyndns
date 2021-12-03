package io.sellmair.ionos.dyndns.util

import io.ktor.utils.io.core.*
import kotlinx.cinterop.*
import platform.posix.*

internal fun posixMkDir(path: String): Boolean {
    return mkdir(path, "0777".toInt(8).convert()) == 0
}

actual val File.exists: Boolean
    get() = access(this.path, F_OK) == 0

actual val File.isDirectory: Boolean
    get() {
        // MPP CORE: I need documentation!
        memScoped {
            val stat = alloc<stat>()
            if (stat(path, stat.ptr) != 0) return false
            return (stat.st_mode.toInt() and S_IFDIR) != 0
        }
    }

actual val File.isFile: Boolean
    get() {
        memScoped {
            val stat = alloc<stat>()
            if (stat(path, stat.ptr) != 0) return false
            return (stat.st_mode.toInt() and S_IFREG) != 0
        }
    }


actual fun File.readText(): String {
    require(isFile) { "File $path is not a file" }
    val handle = fopen(path, "r") ?: error("Can't open file $path")
    try {
        fseek(handle, 0, SEEK_END)
        val length = ftell(handle)
        fseek(handle, 0, SEEK_SET)
        val buffer = ByteArray(length.toInt())
        buffer.usePinned { pinnedBuffer ->
            fread(pinnedBuffer.addressOf(0), 1, length.toULong(), handle)
        }

        return buffer.decodeToString()
    } finally {
        fclose(handle)
    }
}

actual fun File.writeText(text: String) {
    val handle = fopen(path, "w") ?: error("Can't write to file $path")
    val data = text.toByteArray()
    try {
        data.usePinned { pinnedData ->
            val firstDataAddressOrNull = if (data.isEmpty()) null else pinnedData.addressOf(0)
            fwrite(firstDataAddressOrNull, 1, data.size.toULong(), handle).toLong()
        }
    } finally {
        fclose(handle)
    }
}

actual fun File.createDirectory(): Boolean {
    if (this.isDirectory) return true
    if (this.isFile) error("Can't create directory $path: Is already a file!")
    return posixMkDir(path)
}

actual fun File.delete(): Boolean {
    if (!this.exists) return false
    if (this.isDirectory) error("Can't delete directory")
    check(this.isFile) { "Expected file" }
    return remove(path) == 0
}
