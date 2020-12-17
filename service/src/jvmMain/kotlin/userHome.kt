package io.sellmair.ionos.dyndns.service

internal actual val userHome: File
    get() = File(System.getProperty("user.home"))
