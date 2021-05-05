package io.sellmair.ionos.dyndns.update

import io.sellmair.ionos.dyndns.model.Domain
import io.sellmair.ionos.dyndns.util.Logger

sealed class DnsRecordUpdateResult {
    object Success : DnsRecordUpdateResult()

    data class UnknownError(val throwable: Throwable) : DnsRecordUpdateFailure()
    object IonosUnauthorized : DnsRecordUpdateFailure()
    object MissingRootDomain : DnsRecordUpdateFailure()
    object MissingTargetDomain : DnsRecordUpdateFailure()
}

sealed class DnsRecordUpdateFailure : DnsRecordUpdateResult()

fun DnsRecordUpdateFailure.toDiagnostic(domain: Domain): Logger.Diagnostic {
    return Logger.Diagnostic(
        title = "Failed to update ${domain.targetDomainName}",
        details = when (this) {
            is DnsRecordUpdateResult.IonosUnauthorized -> "Ionos unauthorized"
            is DnsRecordUpdateResult.MissingRootDomain -> "Missing root domain: ${domain.rootDomainName}"
            is DnsRecordUpdateResult.MissingTargetDomain -> "Missing target domain: ${domain.targetDomainName}"
            is DnsRecordUpdateResult.UnknownError -> "Unknown error: \n${this.throwable.stackTraceToString()}"
        },
        throwable = if (this is DnsRecordUpdateResult.UnknownError) this.throwable else null
    )
}
