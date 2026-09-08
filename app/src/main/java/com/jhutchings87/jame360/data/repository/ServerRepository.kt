package com.jhutchings87.jame360.data.repository

import com.jhutchings87.jame360.data.model.ServerProfile
import com.jhutchings87.jame360.data.security.SecurePrefs
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import javax.inject.Inject
import javax.inject.Singleton

private const val KEY_PROFILES = "server_profiles"

@Singleton
class ServerRepository @Inject constructor(
    private val securePrefs: SecurePrefs,
    private val json: Json
) {
    private val _profiles = MutableStateFlow(loadProfiles())
    val profiles: StateFlow<List<ServerProfile>> = _profiles.asStateFlow()

    private fun loadProfiles(): List<ServerProfile> {
        val raw = securePrefs.prefs.getString(KEY_PROFILES, null) ?: return emptyList()
        return runCatching { json.decodeFromString<List<ServerProfile>>(raw) }.getOrDefault(emptyList())
    }

    private fun persist(profiles: List<ServerProfile>) {
        securePrefs.prefs.edit()
            .putString(KEY_PROFILES, json.encodeToString(profiles))
            .apply()
        _profiles.value = profiles
    }

    fun upsert(profile: ServerProfile) {
        val current = _profiles.value.toMutableList()
        val index = current.indexOfFirst { it.id == profile.id }
        if (index >= 0) current[index] = profile else current.add(profile)
        persist(current)
    }

    fun delete(profileId: String) {
        persist(_profiles.value.filterNot { it.id == profileId })
    }

    fun get(profileId: String): ServerProfile? = _profiles.value.find { it.id == profileId }
}
