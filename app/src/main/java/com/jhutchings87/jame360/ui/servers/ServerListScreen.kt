package com.jhutchings87.jame360.ui.servers

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Card
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.jhutchings87.jame360.data.model.ServerProfile
import com.jhutchings87.jame360.data.model.ServiceType

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ServerListScreen(
    onOpenDashboard: (String) -> Unit,
    onOpenCalendar: () -> Unit,
    onAddServer: (ServiceType) -> Unit,
    onEditServer: (String) -> Unit,
    viewModel: ServerListViewModel = hiltViewModel()
) {
    val profiles by viewModel.profiles.collectAsState()
    var menuExpanded by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Jame360") },
                actions = {
                    if (profiles.any { it.type == ServiceType.SONARR || it.type == ServiceType.RADARR }) {
                        IconButton(onClick = onOpenCalendar) {
                            Icon(Icons.Default.CalendarMonth, contentDescription = "Upcoming calendar")
                        }
                    }
                }
            )
        },
        floatingActionButton = {
            Box {
                ExtendedFloatingActionButton(
                    text = { Text("Add server") },
                    icon = { Icon(Icons.Default.Add, contentDescription = null) },
                    onClick = { menuExpanded = true }
                )
                DropdownMenu(expanded = menuExpanded, onDismissRequest = { menuExpanded = false }) {
                    ServiceType.entries.forEach { type ->
                        DropdownMenuItem(
                            text = { Text(type.displayName()) },
                            onClick = {
                                menuExpanded = false
                                onAddServer(type)
                            }
                        )
                    }
                }
            }
        }
    ) { padding ->
        if (profiles.isEmpty()) {
            EmptyState(padding)
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentPadding = PaddingValues(vertical = 8.dp)
            ) {
                items(profiles, key = { it.id }) { profile ->
                    ServerRow(
                        profile = profile,
                        onClick = {
                            if (profile.type == ServiceType.NZBGET) onOpenDashboard(profile.id) else onEditServer(profile.id)
                        },
                        onDelete = { viewModel.delete(profile) }
                    )
                }
            }
        }
    }
}

@Composable
private fun EmptyState(padding: PaddingValues) {
    Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("No servers yet")
            Text("Tap \"Add server\" to connect NZBGet, Sonarr, or Radarr")
        }
    }
}

@Composable
private fun ServerRow(profile: ServerProfile, onClick: () -> Unit, onDelete: () -> Unit) {
    Card(modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 4.dp)) {
        ListItem(
            headlineContent = { Text(profile.name) },
            supportingContent = { Text("${profile.type.displayName()} · ${profile.host}:${profile.port}") },
            trailingContent = {
                IconButton(onClick = onDelete) {
                    Icon(Icons.Default.Delete, contentDescription = "Delete server")
                }
            },
            modifier = Modifier.fillMaxWidth().clickable(onClick = onClick)
        )
    }
}

fun ServiceType.displayName(): String = when (this) {
    ServiceType.NZBGET -> "NZBGet"
    ServiceType.SONARR -> "Sonarr"
    ServiceType.RADARR -> "Radarr"
}
