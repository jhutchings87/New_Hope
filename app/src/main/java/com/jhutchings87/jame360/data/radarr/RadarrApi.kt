package com.jhutchings87.jame360.data.radarr

import com.jhutchings87.jame360.data.arr.model.QualityProfile
import com.jhutchings87.jame360.data.arr.model.RootFolder
import com.jhutchings87.jame360.data.radarr.model.RadarrAddMovieRequest
import com.jhutchings87.jame360.data.radarr.model.RadarrCalendarItem
import com.jhutchings87.jame360.data.radarr.model.RadarrMovie
import com.jhutchings87.jame360.data.radarr.model.RadarrSystemStatus
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

/** Radarr v3 REST API. Reference: https://radarr.video/docs/api/ */
interface RadarrApi {

    @GET("api/v3/system/status")
    suspend fun getSystemStatus(): RadarrSystemStatus

    @GET("api/v3/calendar")
    suspend fun getCalendar(
        @Query("start") start: String,
        @Query("end") end: String,
        @Query("unmonitored") unmonitored: Boolean = true
    ): List<RadarrCalendarItem>

    @GET("api/v3/movie/lookup")
    suspend fun lookupMovies(@Query("term") term: String): List<RadarrMovie>

    @GET("api/v3/qualityprofile")
    suspend fun getQualityProfiles(): List<QualityProfile>

    @GET("api/v3/rootfolder")
    suspend fun getRootFolders(): List<RootFolder>

    @POST("api/v3/movie")
    suspend fun addMovie(@Body request: RadarrAddMovieRequest): RadarrMovie
}
