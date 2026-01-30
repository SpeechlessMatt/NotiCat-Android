package com.czy4201b.noticat.core.common.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ServerInfo(
    val name: String,
    val version: String,
    @SerialName("build_time")
    val buildTime: String,
    val owner: String,
    val description: String,
    @SerialName("support_clients")
    val supportClients: List<ClientInfo>
)

@Serializable
data class ClientInfo(
    val client: String,
    val name: String,
    val url: String,
    val description: String,
    val credentials: List<String>,
    val extra: List<ExtraParam>
)

@Serializable
data class ExtraParam(
    val label: String,
    @SerialName("api_key")
    val apiKey: String
)