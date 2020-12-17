package io.sellmair.ionos.dyndns.service

import platform.posix.mkdir
import platform.posix.stat

@Suppress("EXTENSION_SHADOWED_BY_MEMBER") // MPP CORE
actual val stat.st_mode: Number
    get() = this.st_mode.toInt()

actual fun posixMkDir(path: String): Boolean {
    return mkdir(path, "0777".toInt(8).toUShort()) == 0
}
