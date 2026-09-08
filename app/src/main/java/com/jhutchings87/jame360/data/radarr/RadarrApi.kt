package com.jhutchings87.jame360.data.radarr

import com.jhutchings87.jame360.data.radarr.model.RadarrCalendarItem
import com.jhutchings87.jame360.data.radarr.model.RadarrSystemStatus
import retrofit2.http.GET
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
}
