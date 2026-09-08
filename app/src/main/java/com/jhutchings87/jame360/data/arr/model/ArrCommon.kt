package com.jhutchings87.jame360.data.arr.model

import kotlinx.serialization.Serializable

/** Sonarr and Radarr expose identically-shaped quality profiles and root folders. */

@Serializable
data class QualityProfile(
    val id: Int = 0,
    val name: String = ""
)

@Serializable
data class RootFolder(
    val id: Int = 0,
    val path: String = "",
    val freeSpace: Long = 0
)
