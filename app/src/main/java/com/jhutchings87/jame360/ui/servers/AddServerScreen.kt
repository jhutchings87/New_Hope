package com.jhutchings87.jame360.ui.servers

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.jhutchings87.jame360.data.model.ServerProfile
import com.jhutchings87.jame360.data.model.ServiceType

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddServerScreen(
    initialType: ServiceType?,
    editingProfileId: String?,
    onDone: () -> Unit,
    viewModel: ServerFormViewModel = hiltViewModel()
) {
    val existing = remember(editingProfileId) { viewModel.existingProfile(editingProfileId) }
    val type = existing?.type ?: initialType ?: ServiceType.NZBGET

    var name by remember { mutableStateOf(existing?.name ?: type.displayName()) }
    var host by remember { mutableStateOf(existing?.host ?: "") }
    var port by remember { mutableStateOf(existing?.port?.toString() ?: defaultPortFor(type)) }
    var useSsl by remember { mutableStateOf(existing?.useSsl ?: false) }
    var urlBase by remember { mutableStateOf(existing?.urlBase ?: "") }
    var username by remember { mutableStateOf(existing?.username ?: "") }
    var password by remember { mutableStateOf(existing?.password ?: "") }
    var apiKey by remember { mutableStateOf(existing?.apiKey ?: "") }

    val testState by viewModel.testState.collectAsState()

    LaunchedEffect(Unit) { viewModel.resetTestState() }

    fun currentProfile(): ServerProfile = ServerProfile(
        id = existing?.id ?: java.util.UUID.randomUUID().toString(),
        name = name.ifBlank { type.displayName() },
        type = type,
        host = host.trim(),
        port = port.toIntOrNull() ?: defaultPortFor(type).toInt(),
        useSsl = useSsl,
        urlBase = urlBase.trim(),
        username = username.trim(),
        password = password,
        apiKey = apiKey.trim()
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (existing == null) "Add ${type.displayName()}" else "Edit ${existing.name}") },
                navigationIcon = {
                    IconButton(onClick = onDone) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Display name") },
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = host,
                onValueChange = { host = it },
                label = { Text("Host or IP") },
                placeholder = { Text("192.168.1.50") },
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = port,
                onValueChange = { port = it.filter(Char::isDigit) },
                label = { Text("Port") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth()
            )
            Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                Text("Use HTTPS", modifier = Modifier.padding(end = 8.dp))
                Switch(checked = useSsl, onCheckedChange = { useSsl = it })
            }
            OutlinedTextField(
                value = urlBase,
                onValueChange = { urlBase = it },
                label = { Text("URL base (optional)") },
                placeholder = { Text("/nzbget") },
                modifier = Modifier.fillMaxWidth()
            )

            when (type) {
                ServiceType.NZBGET -> {
                    OutlinedTextField(
                        value = username,
                        onValueChange = { username = it },
                        label = { Text("Username") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = password,
                        onValueChange = { password = it },
                        label = { Text("Password") },
                        visualTransformation = PasswordVisualTransformation(),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                ServiceType.SONARR, ServiceType.RADARR -> {
                    OutlinedTextField(
                        value = apiKey,
                        onValueChange = { apiKey = it },
                        label = { Text("API key") },
                        supportingText = { Text("Settings → General → Security in ${type.displayName()}") },
                        visualTransformation = PasswordVisualTransformation(),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            OutlinedButton(
                onClick = { viewModel.testConnection(currentProfile()) },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Test connection")
            }

            when (val state = testState) {
                is ConnectionTestState.Testing -> CircularProgressIndicator(modifier = Modifier.padding(8.dp))
                is ConnectionTestState.Success -> Text("✓ ${state.message}")
                is ConnectionTestState.Failure -> Text("✗ ${state.message}")
                ConnectionTestState.Idle -> Unit
            }

            Button(
                onClick = {
                    viewModel.save(currentProfile())
                    onDone()
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Save")
            }

            if (existing != null) {
                TextButton(
                    onClick = {
                        viewModel.delete(existing.id)
                        onDone()
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Delete server")
                }
            }
        }
    }
}

private fun defaultPortFor(type: ServiceType): String = when (type) {
    ServiceType.NZBGET -> "6789"
    ServiceType.SONARR -> "8989"
    ServiceType.RADARR -> "7878"
}
