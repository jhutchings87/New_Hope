package com.jhutchings87.jame360.data.sonarr

import com.jhutchings87.jame360.data.arr.model.QualityProfile
import com.jhutchings87.jame360.data.arr.model.RootFolder
import com.jhutchings87.jame360.data.sonarr.model.SonarrAddSeriesRequest
import com.jhutchings87.jame360.data.sonarr.model.SonarrCalendarItem
import com.jhutchings87.jame360.data.sonarr.model.SonarrSeries
import com.jhutchings87.jame360.data.sonarr.model.SonarrSystemStatus
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

/** Sonarr v3 REST API. Reference: https://sonarr.tv/docs/api/ */
interface SonarrApi {

    @GET("api/v3/system/status")
    suspend fun getSystemStatus(): SonarrSystemStatus

    @GET("api/v3/calendar")
    suspend fun getCalendar(
        @Query("start") start: String,
        @Query("end") end: String,
        @Query("includeSeries") includeSeries: Boolean = true
    ): List<SonarrCalendarItem>

    @GET("api/v3/series/lookup")
    suspend fun lookupSeries(@Query("term") term: String): List<SonarrSeries>

    @GET("api/v3/qualityprofile")
    suspend fun getQualityProfiles(): List<QualityProfile>

    @GET("api/v3/rootfolder")
    suspend fun getRootFolders(): List<RootFolder>

    @POST("api/v3/series")
    suspend fun addSeries(@Body request: SonarrAddSeriesRequest): SonarrSeries
}
