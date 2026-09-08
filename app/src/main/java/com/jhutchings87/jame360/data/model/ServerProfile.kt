package com.jhutchings87.jame360.data.model

import kotlinx.serialization.Serializable
import java.util.UUID

@Serializable
enum class ServiceType {
    NZBGET,
    SONARR,
    RADARR
}

@Serializable
data class ServerProfile(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val type: ServiceType,
    val host: String,
    val port: Int,
    val useSsl: Boolean = false,
    val urlBase: String = "",
    // NZBGet auth
    val username: String = "",
    val password: String = "",
    // Sonarr / Radarr auth
    val apiKey: String = ""
) {
    val baseUrl: String
        get() {
            val scheme = if (useSsl) "https" else "http"
            val normalizedBase = when {
                urlBase.isBlank() -> ""
                urlBase.startsWith("/") -> urlBase.trimEnd('/')
                else -> "/${urlBase.trim('/')}"
            }
            return "$scheme://$host:$port$normalizedBase"
        }
}
