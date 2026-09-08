package com.jhutchings87.jame360.ui.servers

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jhutchings87.jame360.data.model.ServerProfile
import com.jhutchings87.jame360.data.repository.ServerRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ServerListViewModel @Inject constructor(
    private val serverRepository: ServerRepository
) : ViewModel() {

    val profiles: StateFlow<List<ServerProfile>> = serverRepository.profiles

    fun delete(profile: ServerProfile) {
        viewModelScope.launch { serverRepository.delete(profile.id) }
    }
}
