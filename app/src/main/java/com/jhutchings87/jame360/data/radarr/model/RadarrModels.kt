package com.jhutchings87.jame360.data.radarr.model

import kotlinx.serialization.Serializable

@Serializable
data class RadarrCalendarItem(
    val id: Int,
    val title: String = "",
    val hasFile: Boolean = false,
    val inCinemas: String? = null,
    val physicalRelease: String? = null,
    val digitalRelease: String? = null
) {
    /** Radarr exposes three possible release dates; use whichever is set, preferring digital. */
    val bestReleaseDate: String?
        get() = digitalRelease ?: physicalRelease ?: inCinemas
}

@Serializable
data class RadarrSystemStatus(
    val appName: String = "Radarr",
    val version: String = ""
)
