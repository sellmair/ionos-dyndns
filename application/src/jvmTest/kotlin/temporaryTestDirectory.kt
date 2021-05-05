package io.sellmair.ionos.dyndns

import io.sellmair.ionos.dyndns.util.File
import io.sellmair.ionos.dyndns.util.common
import kotlin.io.path.ExperimentalPathApi
import kotlin.io.path.createTempDirectory

@OptIn(ExperimentalPathApi::class)
actual val temporaryTestDirectory: File
    get() = createTempDirectory("ionos-test").toFile().common