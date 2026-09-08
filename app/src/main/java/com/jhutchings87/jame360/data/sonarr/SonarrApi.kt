package com.jhutchings87.jame360.data.sonarr

import com.jhutchings87.jame360.data.sonarr.model.SonarrCalendarItem
import com.jhutchings87.jame360.data.sonarr.model.SonarrSystemStatus
import retrofit2.http.GET
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
}
