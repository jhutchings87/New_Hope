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

@Serializable
data class SonarrImage(
    val coverType: String = "",
    val remoteUrl: String? = null,
    val url: String? = null
)

/**
 * A series as returned by `series/lookup`. An `id` of 0 means Sonarr does not
 * have it in the library yet, which is how we tell "can add" from "already added".
 */
@Serializable
data class SonarrSeries(
    val id: Int = 0,
    val title: String = "",
    val year: Int = 0,
    val overview: String = "",
    val tvdbId: Int = 0,
    val titleSlug: String = "",
    val monitored: Boolean = false,
    val images: List<SonarrImage> = emptyList()
) {
    val posterUrl: String?
        get() = images.firstOrNull { it.coverType.equals("poster", ignoreCase = true) }
            ?.let { it.remoteUrl ?: it.url }
}

@Serializable
data class SonarrAddOptions(
    val monitor: String = "all",
    val searchForMissingEpisodes: Boolean = true
)

@Serializable
data class SonarrAddSeriesRequest(
    val title: String,
    val tvdbId: Int,
    val titleSlug: String,
    val qualityProfileId: Int,
    val rootFolderPath: String,
    val monitored: Boolean = true,
    val seasonFolder: Boolean = true,
    // Sonarr v3 requires this; Sonarr v4 ignores it. Sending it keeps both happy.
    val languageProfileId: Int = 1,
    val addOptions: SonarrAddOptions = SonarrAddOptions()
)
