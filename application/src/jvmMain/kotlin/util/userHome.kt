package io.sellmair.ionos.dyndns.util

internal actual val userHome: File
    get() = File(System.getProperty("user.home"))
