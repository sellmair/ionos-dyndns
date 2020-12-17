package io.sellmair.ionos.dyndns.service

import kotlin.io.path.ExperimentalPathApi
import kotlin.io.path.createTempDirectory

@OptIn(ExperimentalPathApi::class)
actual val temporaryTestDirectory: File
    get() = createTempDirectory("ionos-test").toFile().common