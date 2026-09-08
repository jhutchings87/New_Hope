package com.jhutchings87.jame360.ui.search

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.jhutchings87.jame360.data.arr.model.SearchResult
import com.jhutchings87.jame360.data.model.ServiceType

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchScreen(viewModel: SearchViewModel = hiltViewModel()) {
    val state by viewModel.state.collectAsState()
    val addSheet by viewModel.addSheet.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(state.message) {
        state.message?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.consumeMessage()
        }
    }

    Scaffold(
        topBar = { TopAppBar(title = { Text("Search") }) },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding)) {
            OutlinedTextField(
                value = state.query,
                onValueChange = viewModel::onQueryChange,
                label = { Text("Show or movie title") },
                singleLine = true,
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                keyboardActions = KeyboardActions(onSearch = { viewModel.search() }),
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp)
            )

            Button(
                onClick = viewModel::search,
                enabled = state.query.isNotBlank() && !state.searching,
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)
            ) {
                Text(if (state.searching) "Searching…" else "Search Sonarr and Radarr")
            }

            when {
                state.noArrServers -> Hint("Add a Sonarr or Radarr server on the Servers tab first.")
                state.searching -> Box(
                    modifier = Modifier.fillMaxWidth().padding(32.dp),
                    contentAlignment = Alignment.Center
                ) { CircularProgressIndicator() }

                state.hasSearched && state.outcome.results.isEmpty() && state.outcome.errors.isEmpty() ->
                    Hint("No matches found.")

                else -> {
                    state.outcome.errors.forEach { Hint(it) }
                    LazyColumn(modifier = Modifier.fillMaxSize()) {
                        items(state.outcome.results, key = { it.key }) { result ->
                            ResultRow(result = result, onAdd = { viewModel.openAddSheet(result) })
                        }
                    }
                }
            }
        }
    }

    addSheet?.let { sheet ->
        val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
        ModalBottomSheet(
            onDismissRequest = viewModel::dismissAddSheet,
            sheetState = sheetState
        ) {
            AddSheetContent(
                sheet = sheet,
                onSelectQualityProfile = viewModel::selectQualityProfile,
                onSelectRootFolder = viewModel::selectRootFolder,
                onConfirm = viewModel::confirmAdd
            )
        }
    }
}

@Composable
private fun Hint(text: String) {
    Text(text, modifier = Modifier.padding(16.dp), style = MaterialTheme.typography.bodyMedium)
}

@Composable
private fun ResultRow(result: SearchResult, onAdd: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 10.dp),
        verticalAlignment = Alignment.Top
    ) {
        AsyncImage(
            model = result.posterUrl,
            contentDescription = null,
            modifier = Modifier
                .width(60.dp)
                .height(90.dp)
                .clip(RoundedCornerShape(6.dp))
        )
        Column(modifier = Modifier.weight(1f).padding(horizontal = 12.dp)) {
            Text(
                if (result.year > 0) "${result.title} (${result.year})" else result.title,
                style = MaterialTheme.typography.bodyLarge,
                maxLines = 2
            )
            Text(
                "${if (result.type == ServiceType.SONARR) "Series" else "Movie"} · ${result.serverName}",
                style = MaterialTheme.typography.bodySmall
            )
            if (result.overview.isNotBlank()) {
                Text(result.overview, style = MaterialTheme.typography.bodySmall, maxLines = 3)
            }
        }
        if (result.alreadyInLibrary) {
            Text("In library", style = MaterialTheme.typography.labelMedium, modifier = Modifier.padding(top = 8.dp))
        } else {
            TextButton(onClick = onAdd) { Text("Add") }
        }
    }
    HorizontalDivider()
}

@Composable
private fun AddSheetContent(
    sheet: AddSheetState,
    onSelectQualityProfile: (Int) -> Unit,
    onSelectRootFolder: (String) -> Unit,
    onConfirm: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp).padding(bottom = 32.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(sheet.result.title, style = MaterialTheme.typography.titleMedium)
        Text("Adding to ${sheet.result.serverName}", style = MaterialTheme.typography.bodySmall)

        when {
            sheet.loading -> Box(
                modifier = Modifier.fillMaxWidth().padding(24.dp),
                contentAlignment = Alignment.Center
            ) { CircularProgressIndicator() }

            sheet.error != null && sheet.targets == null -> Text(sheet.error)

            sheet.targets != null -> {
                val qualityName = sheet.targets.qualityProfiles
                    .firstOrNull { it.id == sheet.selectedQualityProfileId }?.name
                    ?: "Choose quality profile"
                PickerRow(label = qualityName) { dismiss ->
                    sheet.targets.qualityProfiles.forEach { profile ->
                        DropdownMenuItem(
                            text = { Text(profile.name) },
                            onClick = {
                                onSelectQualityProfile(profile.id)
                                dismiss()
                            }
                        )
                    }
                }

                val folderName = sheet.selectedRootFolderPath ?: "Choose root folder"
                PickerRow(label = folderName) { dismiss ->
                    sheet.targets.rootFolders.forEach { folder ->
                        DropdownMenuItem(
                            text = { Text(folder.path) },
                            onClick = {
                                onSelectRootFolder(folder.path)
                                dismiss()
                            }
                        )
                    }
                }

                sheet.error?.let { Text(it, style = MaterialTheme.typography.bodySmall) }

                Button(
                    onClick = onConfirm,
                    enabled = sheet.canSubmit,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(if (sheet.submitting) "Adding…" else "Add and search")
                }
            }
        }
    }
}

@Composable
private fun PickerRow(label: String, menuItems: @Composable (dismiss: () -> Unit) -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    Box(modifier = Modifier.fillMaxWidth()) {
        OutlinedButton(onClick = { expanded = true }, modifier = Modifier.fillMaxWidth()) {
            Text(label, maxLines = 1)
        }
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            menuItems { expanded = false }
        }
    }
}
