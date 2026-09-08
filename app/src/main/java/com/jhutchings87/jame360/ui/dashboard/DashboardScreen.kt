package com.jhutchings87.jame360.ui.dashboard

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.PauseCircle
import androidx.compose.material.icons.filled.PlayCircle
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.jhutchings87.jame360.data.nzbget.model.NzbGetGroup
import com.jhutchings87.jame360.data.nzbget.model.NzbGetHistoryItem
import com.jhutchings87.jame360.data.nzbget.model.NzbGetStatus
import com.jhutchings87.jame360.ui.common.UiState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    profileId: String,
    onBack: () -> Unit,
    viewModel: DashboardViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text((state as? UiState.Success)?.data?.profile?.name ?: "NZBGet") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        when (val current = state) {
            is UiState.Loading -> Row(
                modifier = Modifier.fillMaxSize().padding(padding),
                horizontalArrangement = Arrangement.Center
            ) { CircularProgressIndicator(modifier = Modifier.padding(32.dp)) }

            is UiState.Error -> Column(modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp)) {
                Text("Couldn't reach NZBGet")
                Text(current.message)
            }

            is UiState.Success -> DashboardContent(
                data = current.data,
                modifier = Modifier.fillMaxSize().padding(padding),
                onToggleGlobalPause = { viewModel.toggleGlobalPause(current.data.status.downloadPaused) },
                onPauseGroup = viewModel::pauseGroup,
                onResumeGroup = viewModel::resumeGroup,
                onDeleteGroup = viewModel::deleteGroup
            )
        }
    }
}

@Composable
private fun DashboardContent(
    data: DashboardData,
    modifier: Modifier = Modifier,
    onToggleGlobalPause: () -> Unit,
    onPauseGroup: (Int) -> Unit,
    onResumeGroup: (Int) -> Unit,
    onDeleteGroup: (Int) -> Unit
) {
    LazyColumn(modifier = modifier, contentPadding = androidx.compose.foundation.layout.PaddingValues(bottom = 16.dp)) {
        item { StatusCard(status = data.status, onToggleGlobalPause = onToggleGlobalPause) }

        if (data.groups.isNotEmpty()) {
            item { SectionHeader("Queue") }
            items(data.groups, key = { it.nzbId }) { group ->
                QueueRow(
                    group = group,
                    onPause = { onPauseGroup(group.nzbId) },
                    onResume = { onResumeGroup(group.nzbId) },
                    onDelete = { onDeleteGroup(group.nzbId) }
                )
            }
        } else {
            item { SectionHeader("Queue") }
            item { Text("Nothing downloading", modifier = Modifier.padding(16.dp)) }
        }

        if (data.history.isNotEmpty()) {
            item { SectionHeader("Recent history") }
            items(data.history, key = { it.nzbId }) { HistoryRow(it) }
        }
    }
}

@Composable
private fun StatusCard(status: NzbGetStatus, onToggleGlobalPause: () -> Unit) {
    Card(modifier = Modifier.fillMaxWidth().padding(12.dp)) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    if (status.downloadPaused) "Paused" else if (status.serverStandBy) "Idle" else "Downloading",
                    style = androidx.compose.material3.MaterialTheme.typography.titleMedium
                )
                IconButton(onClick = onToggleGlobalPause) {
                    Icon(
                        if (status.downloadPaused) Icons.Default.PlayCircle else Icons.Default.PauseCircle,
                        contentDescription = if (status.downloadPaused) "Resume all" else "Pause all"
                    )
                }
            }
            Text("Speed: ${formatRate(status.downloadRateBps)}")
            Text("Remaining: ${formatMb(status.remainingSizeMB)}  ·  Downloaded: ${formatMb(status.downloadedSizeMB)}")
            Text("Free disk space: ${formatMb(status.freeDiskSpaceMB)}")
        }
    }
}

@Composable
private fun SectionHeader(title: String) {
    Text(
        title,
        style = androidx.compose.material3.MaterialTheme.typography.labelLarge,
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
    )
}

@Composable
private fun QueueRow(group: NzbGetGroup, onPause: () -> Unit, onResume: () -> Unit, onDelete: () -> Unit) {
    Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 6.dp)) {
        Text(group.name, maxLines = 1)
        LinearProgressIndicator(
            progress = { group.progressFraction },
            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("${formatMb(group.fileSizeMB - group.remainingSizeMB)} / ${formatMb(group.fileSizeMB)}  ·  ${group.status}")
            Row {
                IconButton(onClick = if (group.isPaused) onResume else onPause) {
                    Icon(
                        if (group.isPaused) Icons.Default.PlayCircle else Icons.Default.PauseCircle,
                        contentDescription = if (group.isPaused) "Resume" else "Pause"
                    )
                }
                IconButton(onClick = onDelete) {
                    Icon(Icons.Default.Delete, contentDescription = "Delete")
                }
            }
        }
    }
    HorizontalDivider()
}

@Composable
private fun HistoryRow(item: NzbGetHistoryItem) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(item.name, maxLines = 1, modifier = Modifier.padding(end = 8.dp))
        Text(item.status)
    }
}

private fun formatMb(mb: Long): String = if (mb >= 1024) "%.1f GB".format(mb / 1024.0) else "$mb MB"

private fun formatRate(bytesPerSec: Long): String {
    val kbps = bytesPerSec / 1024.0
    return if (kbps >= 1024) "%.1f MB/s".format(kbps / 1024.0) else "%.0f KB/s".format(kbps)
}
