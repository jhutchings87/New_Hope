package com.jhutchings87.jame360.data.sonarr.model

import kotlinx.serialization.Serializable

@Serializable
data class SonarrSeriesRef(
    val title: String = ""
)

@Serializable
data class SonarrCalendarItem(
    val id: Int,
    val seriesId: Int = 0,
    val title: String = "",
    val airDateUtc: String? = null,
    val seasonNumber: Int = 0,
    val episodeNumber: Int = 0,
    val hasFile: Boolean = false,
    val series: SonarrSeriesRef? = null
)

@Serializable
data class SonarrSystemStatus(
    val appName: String = "Sonarr",
    val version: String = ""
)
