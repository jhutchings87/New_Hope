package com.jhutchings87.jame360.ui.calendar

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
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
import com.jhutchings87.jame360.ui.common.UiState
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalendarScreen(
    onBack: () -> Unit,
    viewModel: CalendarViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Upcoming") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        when (val current = state) {
            is UiState.Loading -> Text("Loading…", modifier = Modifier.padding(padding).padding(16.dp))
            is UiState.Error -> Text(current.message, modifier = Modifier.padding(padding).padding(16.dp))
            is UiState.Success -> {
                if (current.data.isEmpty()) {
                    Text(
                        "Nothing scheduled. Add a Sonarr or Radarr server first.",
                        modifier = Modifier.padding(padding).padding(16.dp)
                    )
                } else {
                    LazyColumn(modifier = Modifier.fillMaxSize().padding(padding)) {
                        items(current.data) { entry -> CalendarRow(entry) }
                    }
                }
            }
        }
    }
}

private val dateFormatter: DateTimeFormatter = DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM)

@Composable
private fun CalendarRow(entry: CalendarEntry) {
    Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.padding(end = 8.dp)) {
                Text(entry.title, style = MaterialTheme.typography.bodyLarge, maxLines = 1)
                Text(
                    "${entry.subtitle} · ${entry.date?.format(dateFormatter) ?: "Date TBD"} · ${entry.serverName}",
                    style = MaterialTheme.typography.bodySmall
                )
            }
            Icon(
                if (entry.hasFile) Icons.Default.CheckCircle else Icons.Default.RadioButtonUnchecked,
                contentDescription = if (entry.hasFile) "Downloaded" else "Not yet downloaded"
            )
        }
    }
    HorizontalDivider()
}
