package com.jhutchings87.jame360.ui.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jhutchings87.jame360.data.model.ServerProfile
import com.jhutchings87.jame360.data.nzbget.NzbGetClient
import com.jhutchings87.jame360.data.nzbget.model.NzbGetGroup
import com.jhutchings87.jame360.data.nzbget.model.NzbGetHistoryItem
import com.jhutchings87.jame360.data.nzbget.model.NzbGetStatus
import com.jhutchings87.jame360.data.repository.ServerRepository
import com.jhutchings87.jame360.ui.common.UiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import okhttp3.OkHttpClient
import javax.inject.Inject

private const val REFRESH_INTERVAL_MS = 5_000L

data class DashboardData(
    val profile: ServerProfile,
    val status: NzbGetStatus,
    val groups: List<NzbGetGroup>,
    val history: List<NzbGetHistoryItem>
)

@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val serverRepository: ServerRepository,
    private val okHttpClient: OkHttpClient,
    private val json: Json
) : ViewModel() {

    private var profileId: String? = null
    private var pollJob: Job? = null

    private val _state = MutableStateFlow<UiState<DashboardData>>(UiState.Loading)
    val state: StateFlow<UiState<DashboardData>> = _state.asStateFlow()

    /**
     * Point the dashboard at an NZBGet server and start polling it. Safe to call
     * on every recomposition; re-binding only restarts polling when the target
     * actually changes.
     */
    fun bind(profileId: String) {
        if (this.profileId == profileId && pollJob?.isActive == true) return
        this.profileId = profileId
        _state.value = UiState.Loading
        pollJob?.cancel()
        pollJob = viewModelScope.launch {
            while (isActive) {
                refresh()
                delay(REFRESH_INTERVAL_MS)
            }
        }
    }

    // Resolved per call rather than cached, so edits to the server profile
    // (host, password) take effect on the next refresh instead of needing a restart.
    private fun client(): Pair<ServerProfile, NzbGetClient>? {
        val profile = profileId?.let { serverRepository.get(it) } ?: return null
        return profile to NzbGetClient(profile, okHttpClient, json)
    }

    fun refresh() {
        viewModelScope.launch {
            val (profile, nzbGet) = client() ?: run {
                _state.value = UiState.Error("Server no longer configured")
                return@launch
            }
            runCatching {
                DashboardData(
                    profile = profile,
                    status = nzbGet.status(),
                    groups = nzbGet.listGroups(),
                    history = nzbGet.history().take(20)
                )
            }.fold(
                onSuccess = { _state.value = UiState.Success(it) },
                onFailure = { _state.value = UiState.Error(it.message ?: "Failed to reach NZBGet") }
            )
        }
    }

    fun toggleGlobalPause(currentlyPaused: Boolean) {
        act { if (currentlyPaused) it.resumeDownload() else it.pauseDownload() }
    }

    fun pauseGroup(nzbId: Int) = act { it.pauseGroup(nzbId) }
    fun resumeGroup(nzbId: Int) = act { it.resumeGroup(nzbId) }
    fun deleteGroup(nzbId: Int) = act { it.deleteGroup(nzbId) }

    private fun act(block: suspend (NzbGetClient) -> Boolean) {
        val nzbGet = client()?.second ?: return
        viewModelScope.launch {
            runCatching { block(nzbGet) }
            refresh()
        }
    }
}
