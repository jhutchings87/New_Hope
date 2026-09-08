package com.jhutchings87.jame360.data.arr

import com.jhutchings87.jame360.data.model.ServerProfile
import com.jhutchings87.jame360.data.radarr.RadarrApi
import com.jhutchings87.jame360.data.sonarr.SonarrApi
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Sonarr and Radarr share the same *arr REST shape (X-Api-Key header, /api/v3/... paths),
 * so a single factory builds a per-profile Retrofit instance for either.
 */
@Singleton
class ArrApiFactory @Inject constructor(
    private val okHttpClient: OkHttpClient,
    private val json: Json
) {
    private fun retrofitFor(profile: ServerProfile): Retrofit {
        val client = okHttpClient.newBuilder()
            .addInterceptor { chain ->
                val request = chain.request().newBuilder()
                    .addHeader("X-Api-Key", profile.apiKey)
                    .build()
                chain.proceed(request)
            }
            .build()

        return Retrofit.Builder()
            .baseUrl("${profile.baseUrl}/")
            .client(client)
            .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
            .build()
    }

    fun sonarr(profile: ServerProfile): SonarrApi = retrofitFor(profile).create(SonarrApi::class.java)

    fun radarr(profile: ServerProfile): RadarrApi = retrofitFor(profile).create(RadarrApi::class.java)
}
