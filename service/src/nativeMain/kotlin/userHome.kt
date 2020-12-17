package io.sellmair.ionos.dyndns.service

import io.sellmair.ionos.dyndns.cli.environmentVariable

internal actual val userHome: File get() = File(environmentVariable("HOME"))
