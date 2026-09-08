package com.jhutchings87.jame360.ui.servers

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jhutchings87.jame360.data.arr.ArrApiFactory
import com.jhutchings87.jame360.data.model.ServerProfile
import com.jhutchings87.jame360.data.model.ServiceType
import com.jhutchings87.jame360.data.nzbget.NzbGetClient
import com.jhutchings87.jame360.data.repository.ServerRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import okhttp3.OkHttpClient
import javax.inject.Inject

sealed interface ConnectionTestState {
    data object Idle : ConnectionTestState
    data object Testing : ConnectionTestState
    data class Success(val message: String) : ConnectionTestState
    data class Failure(val message: String) : ConnectionTestState
}

@HiltViewModel
class ServerFormViewModel @Inject constructor(
    private val serverRepository: ServerRepository,
    private val arrApiFactory: ArrApiFactory,
    private val okHttpClient: OkHttpClient,
    private val json: Json
) : ViewModel() {

    private val _testState = MutableStateFlow<ConnectionTestState>(ConnectionTestState.Idle)
    val testState: StateFlow<ConnectionTestState> = _testState.asStateFlow()

    fun existingProfile(profileId: String?): ServerProfile? =
        profileId?.let { serverRepository.get(it) }

    fun save(profile: ServerProfile) {
        serverRepository.upsert(profile)
    }

    fun delete(profileId: String) {
        serverRepository.delete(profileId)
    }

    fun testConnection(profile: ServerProfile) {
        _testState.value = ConnectionTestState.Testing
        viewModelScope.launch {
            _testState.value = runCatching {
                when (profile.type) {
                    ServiceType.NZBGET -> {
                        val status = NzbGetClient(profile, okHttpClient, json).status()
                        "Connected. Free disk space: ${status.freeDiskSpaceMB} MB"
                    }
                    ServiceType.SONARR -> {
                        val status = arrApiFactory.sonarr(profile).getSystemStatus()
                        "Connected to ${status.appName} ${status.version}"
                    }
                    ServiceType.RADARR -> {
                        val status = arrApiFactory.radarr(profile).getSystemStatus()
                        "Connected to ${status.appName} ${status.version}"
                    }
                }
            }.fold(
                onSuccess = { ConnectionTestState.Success(it) },
                onFailure = { ConnectionTestState.Failure(it.message ?: "Connection failed") }
            )
        }
    }

    fun resetTestState() {
        _testState.value = ConnectionTestState.Idle
    }
}
