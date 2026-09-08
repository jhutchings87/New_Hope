package com.jhutchings87.jame360.ui.search

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jhutchings87.jame360.data.arr.ArrRepository
import com.jhutchings87.jame360.data.arr.model.AddTargets
import com.jhutchings87.jame360.data.arr.model.SearchOutcome
import com.jhutchings87.jame360.data.arr.model.SearchResult
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SearchUiState(
    val query: String = "",
    val searching: Boolean = false,
    val hasSearched: Boolean = false,
    val outcome: SearchOutcome = SearchOutcome(emptyList(), emptyList()),
    val noArrServers: Boolean = false,
    val message: String? = null
)

/** State for the "add this to my library" sheet. */
data class AddSheetState(
    val result: SearchResult,
    val loading: Boolean = true,
    val targets: AddTargets? = null,
    val selectedQualityProfileId: Int? = null,
    val selectedRootFolderPath: String? = null,
    val submitting: Boolean = false,
    val error: String? = null
) {
    val canSubmit: Boolean
        get() = !loading && !submitting && selectedQualityProfileId != null && selectedRootFolderPath != null
}

@HiltViewModel
class SearchViewModel @Inject constructor(
    private val arrRepository: ArrRepository
) : ViewModel() {

    private val _state = MutableStateFlow(SearchUiState())
    val state: StateFlow<SearchUiState> = _state.asStateFlow()

    private val _addSheet = MutableStateFlow<AddSheetState?>(null)
    val addSheet: StateFlow<AddSheetState?> = _addSheet.asStateFlow()

    fun onQueryChange(query: String) {
        _state.update { it.copy(query = query) }
    }

    fun search() {
        val term = _state.value.query.trim()
        if (term.isBlank()) return

        if (arrRepository.arrProfiles().isEmpty()) {
            _state.update { it.copy(noArrServers = true, hasSearched = true) }
            return
        }

        _state.update { it.copy(searching = true, noArrServers = false) }
        viewModelScope.launch {
            val outcome = runCatching { arrRepository.search(term) }
                .getOrElse { SearchOutcome(emptyList(), listOf(it.message ?: "Search failed")) }
            _state.update {
                it.copy(searching = false, hasSearched = true, outcome = outcome)
            }
        }
    }

    fun openAddSheet(result: SearchResult) {
        _addSheet.value = AddSheetState(result = result)
        viewModelScope.launch {
            arrRepository.loadAddTargets(result.serverId).fold(
                onSuccess = { targets ->
                    _addSheet.update { current ->
                        current?.copy(
                            loading = false,
                            targets = targets,
                            selectedQualityProfileId = targets.qualityProfiles.firstOrNull()?.id,
                            selectedRootFolderPath = targets.rootFolders.firstOrNull()?.path
                        )
                    }
                },
                onFailure = { error ->
                    _addSheet.update { current ->
                        current?.copy(loading = false, error = error.message ?: "Couldn't load options")
                    }
                }
            )
        }
    }

    fun selectQualityProfile(id: Int) {
        _addSheet.update { it?.copy(selectedQualityProfileId = id) }
    }

    fun selectRootFolder(path: String) {
        _addSheet.update { it?.copy(selectedRootFolderPath = path) }
    }

    fun dismissAddSheet() {
        _addSheet.value = null
    }

    fun confirmAdd() {
        val sheet = _addSheet.value ?: return
        val qualityProfileId = sheet.selectedQualityProfileId ?: return
        val rootFolderPath = sheet.selectedRootFolderPath ?: return

        _addSheet.update { it?.copy(submitting = true, error = null) }
        viewModelScope.launch {
            arrRepository.add(sheet.result, qualityProfileId, rootFolderPath).fold(
                onSuccess = { message ->
                    _addSheet.value = null
                    _state.update { it.copy(message = message) }
                    markAdded(sheet.result)
                },
                onFailure = { error ->
                    _addSheet.update {
                        it?.copy(submitting = false, error = error.message ?: "Add failed")
                    }
                }
            )
        }
    }

    /** Flip the row to "In library" without re-running the whole search. */
    private fun markAdded(added: SearchResult) {
        _state.update { current ->
            val updated = current.outcome.results.map { result ->
                if (result.key == added.key) result.copy(alreadyInLibrary = true) else result
            }
            current.copy(outcome = current.outcome.copy(results = updated))
        }
    }

    fun consumeMessage() {
        _state.update { it.copy(message = null) }
    }
}
