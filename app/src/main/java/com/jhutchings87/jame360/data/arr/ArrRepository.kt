package com.jhutchings87.jame360.data.arr

import com.jhutchings87.jame360.data.arr.model.AddPayload
import com.jhutchings87.jame360.data.arr.model.AddTargets
import com.jhutchings87.jame360.data.arr.model.SearchOutcome
import com.jhutchings87.jame360.data.arr.model.SearchResult
import com.jhutchings87.jame360.data.model.ServerProfile
import com.jhutchings87.jame360.data.model.ServiceType
import com.jhutchings87.jame360.data.radarr.model.RadarrAddMovieRequest
import com.jhutchings87.jame360.data.repository.ServerRepository
import com.jhutchings87.jame360.data.sonarr.model.SonarrAddSeriesRequest
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Search and library-add across every configured Sonarr/Radarr server.
 * Each server is queried in parallel; one unreachable server degrades that
 * server's results into an error message instead of failing the whole search.
 */
@Singleton
class ArrRepository @Inject constructor(
    private val serverRepository: ServerRepository,
    private val arrApiFactory: ArrApiFactory
) {

    fun arrProfiles(): List<ServerProfile> = serverRepository.profiles.value
        .filter { it.type == ServiceType.SONARR || it.type == ServiceType.RADARR }

    suspend fun search(term: String): SearchOutcome = coroutineScope {
        val profiles = arrProfiles()
        if (term.isBlank() || profiles.isEmpty()) return@coroutineScope SearchOutcome(emptyList(), emptyList())

        val perServer = profiles.map { profile ->
            async { profile to runCatching { searchOne(profile, term) } }
        }.map { it.await() }

        val results = mutableListOf<SearchResult>()
        val errors = mutableListOf<String>()
        perServer.forEach { (profile, outcome) ->
            outcome.fold(
                onSuccess = { results += it },
                onFailure = { errors += "${profile.name}: ${it.message ?: "unreachable"}" }
            )
        }
        SearchOutcome(results, errors)
    }

    private suspend fun searchOne(profile: ServerProfile, term: String): List<SearchResult> =
        when (profile.type) {
            ServiceType.SONARR -> arrApiFactory.sonarr(profile).lookupSeries(term).map { series ->
                SearchResult(
                    title = series.title,
                    year = series.year,
                    overview = series.overview,
                    posterUrl = series.posterUrl,
                    type = ServiceType.SONARR,
                    serverId = profile.id,
                    serverName = profile.name,
                    alreadyInLibrary = series.id > 0,
                    payload = AddPayload.Series(series.tvdbId, series.titleSlug)
                )
            }

            ServiceType.RADARR -> arrApiFactory.radarr(profile).lookupMovies(term).map { movie ->
                SearchResult(
                    title = movie.title,
                    year = movie.year,
                    overview = movie.overview,
                    posterUrl = movie.posterUrl,
                    type = ServiceType.RADARR,
                    serverId = profile.id,
                    serverName = profile.name,
                    alreadyInLibrary = movie.id > 0,
                    payload = AddPayload.Movie(movie.tmdbId, movie.titleSlug)
                )
            }

            ServiceType.NZBGET -> emptyList()
        }

    suspend fun loadAddTargets(serverId: String): Result<AddTargets> = runCatching {
        val profile = serverRepository.get(serverId) ?: error("Server no longer configured")
        coroutineScope {
            when (profile.type) {
                ServiceType.SONARR -> {
                    val api = arrApiFactory.sonarr(profile)
                    val profiles = async { api.getQualityProfiles() }
                    val folders = async { api.getRootFolders() }
                    AddTargets(profiles.await(), folders.await())
                }

                ServiceType.RADARR -> {
                    val api = arrApiFactory.radarr(profile)
                    val profiles = async { api.getQualityProfiles() }
                    val folders = async { api.getRootFolders() }
                    AddTargets(profiles.await(), folders.await())
                }

                ServiceType.NZBGET -> error("NZBGet has no library to add to")
            }
        }
    }

    suspend fun add(
        result: SearchResult,
        qualityProfileId: Int,
        rootFolderPath: String
    ): Result<String> = runCatching {
        val profile = serverRepository.get(result.serverId) ?: error("Server no longer configured")
        when (val payload = result.payload) {
            is AddPayload.Series -> {
                arrApiFactory.sonarr(profile).addSeries(
                    SonarrAddSeriesRequest(
                        title = result.title,
                        tvdbId = payload.tvdbId,
                        titleSlug = payload.titleSlug,
                        qualityProfileId = qualityProfileId,
                        rootFolderPath = rootFolderPath
                    )
                )
                "Added ${result.title} to ${result.serverName}"
            }

            is AddPayload.Movie -> {
                arrApiFactory.radarr(profile).addMovie(
                    RadarrAddMovieRequest(
                        title = result.title,
                        tmdbId = payload.tmdbId,
                        titleSlug = payload.titleSlug,
                        qualityProfileId = qualityProfileId,
                        rootFolderPath = rootFolderPath
                    )
                )
                "Added ${result.title} to ${result.serverName}"
            }
        }
    }
}
