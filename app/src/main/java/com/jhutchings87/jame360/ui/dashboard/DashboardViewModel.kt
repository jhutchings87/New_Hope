package com.jhutchings87.jame360.ui.dashboard

import androidx.lifecycle.SavedStateHandle
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
    savedStateHandle: SavedStateHandle,
    private val serverRepository: ServerRepository,
    private val okHttpClient: OkHttpClient,
    private val json: Json
) : ViewModel() {

    private val profileId: String = checkNotNull(savedStateHandle["profileId"])

    private val client: NzbGetClient? by lazy {
        serverRepository.get(profileId)?.let { NzbGetClient(it, okHttpClient, json) }
    }

    private val _state = MutableStateFlow<UiState<DashboardData>>(UiState.Loading)
    val state: StateFlow<UiState<DashboardData>> = _state.asStateFlow()

    init {
        startPolling()
    }

    private fun startPolling() {
        viewModelScope.launch {
            while (isActive) {
                refresh()
                kotlinx.coroutines.delay(REFRESH_INTERVAL_MS)
            }
        }
    }

    fun refresh() {
        viewModelScope.launch {
            val profile = serverRepository.get(profileId)
            val nzbGet = client
            if (profile == null || nzbGet == null) {
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
        val nzbGet = client ?: return
        viewModelScope.launch {
            runCatching { if (currentlyPaused) nzbGet.resumeDownload() else nzbGet.pauseDownload() }
            refresh()
        }
    }

    fun pauseGroup(nzbId: Int) = act { it.pauseGroup(nzbId) }
    fun resumeGroup(nzbId: Int) = act { it.resumeGroup(nzbId) }
    fun deleteGroup(nzbId: Int) = act { it.deleteGroup(nzbId) }

    private fun act(block: suspend (NzbGetClient) -> Boolean) {
        val nzbGet = client ?: return
        viewModelScope.launch {
            runCatching { block(nzbGet) }
            refresh()
        }
    }
}
