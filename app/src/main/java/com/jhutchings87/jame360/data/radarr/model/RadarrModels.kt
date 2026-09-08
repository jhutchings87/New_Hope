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

@Serializable
data class RadarrImage(
    val coverType: String = "",
    val remoteUrl: String? = null,
    val url: String? = null
)

/**
 * A movie as returned by `movie/lookup`. An `id` of 0 means Radarr does not have
 * it in the library yet, which is how we tell "can add" from "already added".
 */
@Serializable
data class RadarrMovie(
    val id: Int = 0,
    val title: String = "",
    val year: Int = 0,
    val overview: String = "",
    val tmdbId: Int = 0,
    val titleSlug: String = "",
    val monitored: Boolean = false,
    val images: List<RadarrImage> = emptyList()
) {
    val posterUrl: String?
        get() = images.firstOrNull { it.coverType.equals("poster", ignoreCase = true) }
            ?.let { it.remoteUrl ?: it.url }
}

@Serializable
data class RadarrAddOptions(
    val searchForMovie: Boolean = true
)

@Serializable
data class RadarrAddMovieRequest(
    val title: String,
    val tmdbId: Int,
    val titleSlug: String,
    val qualityProfileId: Int,
    val rootFolderPath: String,
    val monitored: Boolean = true,
    val minimumAvailability: String = "released",
    val addOptions: RadarrAddOptions = RadarrAddOptions()
)
