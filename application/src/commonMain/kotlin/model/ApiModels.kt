package io.sellmair.ionos.dyndns.model

import kotlinx.serialization.Serializable

@Serializable
internal data class ZoneDescriptorDTO(val name: String, val id: String, val type: String)

@Serializable
internal data class ZoneDTO(val name: String, val id: String, val records: List<DnsRecordDTO>)

@Serializable
internal data class DnsRecordDTO(
    val id: String,
    val name: String,
    val rootName: String,
    val type: String,
    val content: String,
    val ttl: Int,
    val disabled: Boolean,
    val prio: Int = 0,
)

@Serializable
internal data class DnsRecordUpdateDTO(
    val content: String,
    val ttl: Int,
    val disabled: Boolean,
    val prio: Int
)

internal fun DnsRecordDTO.toDnsRecordUpdateDTO(): DnsRecordUpdateDTO {
    return DnsRecordUpdateDTO(
        content = this.content,
        ttl = this.ttl,
        disabled = this.disabled,
        prio = this.prio
    )
}