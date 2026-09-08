package com.jhutchings87.jame360.ui.downloads

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
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
import com.jhutchings87.jame360.data.model.ServiceType
import com.jhutchings87.jame360.ui.dashboard.DashboardBody
import com.jhutchings87.jame360.ui.servers.ServerListViewModel

/**
 * The Downloads tab. Resolves which NZBGet server to show: straight to the
 * queue when there's only one, with a chip row to switch when there are more.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DownloadsScreen(viewModel: ServerListViewModel = hiltViewModel()) {
    val profiles by viewModel.profiles.collectAsState()
    val nzbGetServers = profiles.filter { it.type == ServiceType.NZBGET }

    var selectedId by remember { mutableStateOf<String?>(null) }
    val selected = nzbGetServers.firstOrNull { it.id == selectedId } ?: nzbGetServers.firstOrNull()

    Scaffold(
        topBar = { TopAppBar(title = { Text(selected?.name ?: "Downloads") }) }
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding)) {
            if (nzbGetServers.size > 1) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState())
                        .padding(horizontal = 12.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    nzbGetServers.forEach { server ->
                        FilterChip(
                            selected = server.id == selected?.id,
                            onClick = { selectedId = server.id },
                            label = { Text(server.name) }
                        )
                    }
                }
            }

            if (selected == null) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(
                        "Add an NZBGet server on the Servers tab to see your queue here.",
                        modifier = Modifier.padding(24.dp)
                    )
                }
            } else {
                DashboardBody(profileId = selected.id, modifier = Modifier.fillMaxSize())
            }
        }
    }
}
