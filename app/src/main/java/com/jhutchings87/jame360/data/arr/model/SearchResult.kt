package com.jhutchings87.jame360.data.arr.model

import com.jhutchings87.jame360.data.model.ServiceType

/** What Jame360 needs to add an item back to the server it came from. */
sealed interface AddPayload {
    data class Series(val tvdbId: Int, val titleSlug: String) : AddPayload
    data class Movie(val tmdbId: Int, val titleSlug: String) : AddPayload
}

/** A Sonarr series or Radarr movie found by search, normalized for the UI. */
data class SearchResult(
    val title: String,
    val year: Int,
    val overview: String,
    val posterUrl: String?,
    val type: ServiceType,
    val serverId: String,
    val serverName: String,
    val alreadyInLibrary: Boolean,
    val payload: AddPayload
) {
    val key: String
        get() = when (payload) {
            is AddPayload.Series -> "$serverId-series-${payload.tvdbId}"
            is AddPayload.Movie -> "$serverId-movie-${payload.tmdbId}"
        }
}

/** Search fans out to every configured server, so partial failure is normal. */
data class SearchOutcome(
    val results: List<SearchResult>,
    val errors: List<String>
)

/** The choices a user must make before an item can be added. */
data class AddTargets(
    val qualityProfiles: List<QualityProfile>,
    val rootFolders: List<RootFolder>
)
