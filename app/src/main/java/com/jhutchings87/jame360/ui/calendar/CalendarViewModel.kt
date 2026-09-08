package com.jhutchings87.jame360.ui.calendar

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jhutchings87.jame360.data.arr.ArrApiFactory
import com.jhutchings87.jame360.data.model.ServiceType
import com.jhutchings87.jame360.data.repository.ServerRepository
import com.jhutchings87.jame360.ui.common.UiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import javax.inject.Inject

@HiltViewModel
class CalendarViewModel @Inject constructor(
    private val serverRepository: ServerRepository,
    private val arrApiFactory: ArrApiFactory
) : ViewModel() {

    private val _state = MutableStateFlow<UiState<List<CalendarEntry>>>(UiState.Loading)
    val state: StateFlow<UiState<List<CalendarEntry>>> = _state.asStateFlow()

    init {
        refresh()
    }

    fun refresh() {
        _state.value = UiState.Loading
        viewModelScope.launch {
            val profiles = serverRepository.profiles.value.filter {
                it.type == ServiceType.SONARR || it.type == ServiceType.RADARR
            }

            if (profiles.isEmpty()) {
                _state.value = UiState.Success(emptyList())
                return@launch
            }

            val start = LocalDate.now().minusDays(3).format(DateTimeFormatter.ISO_LOCAL_DATE)
            val end = LocalDate.now().plusDays(14).format(DateTimeFormatter.ISO_LOCAL_DATE)

            val results = profiles.map { profile ->
                async {
                    runCatching {
                        when (profile.type) {
                            ServiceType.SONARR -> arrApiFactory.sonarr(profile).getCalendar(start, end).map { item ->
                                CalendarEntry(
                                    date = item.airDateUtc?.let { runCatching { java.time.Instant.parse(it).atZone(java.time.ZoneId.systemDefault()).toLocalDate() }.getOrNull() },
                                    title = item.series?.title?.takeIf { it.isNotBlank() } ?: item.title,
                                    subtitle = "S${item.seasonNumber.toString().padStart(2, '0')}E${item.episodeNumber.toString().padStart(2, '0')}",
                                    hasFile = item.hasFile,
                                    source = ServiceType.SONARR,
                                    serverName = profile.name
                                )
                            }
                            ServiceType.RADARR -> arrApiFactory.radarr(profile).getCalendar(start, end).map { item ->
                                CalendarEntry(
                                    date = item.bestReleaseDate?.let { runCatching { LocalDate.parse(it.take(10)) }.getOrNull() },
                                    title = item.title,
                                    subtitle = "Movie",
                                    hasFile = item.hasFile,
                                    source = ServiceType.RADARR,
                                    serverName = profile.name
                                )
                            }
                            ServiceType.NZBGET -> emptyList()
                        }
                    }.getOrDefault(emptyList())
                }
            }.awaitAll().flatten().sortedBy { it.date ?: LocalDate.MAX }

            _state.value = UiState.Success(results)
        }
    }
}
