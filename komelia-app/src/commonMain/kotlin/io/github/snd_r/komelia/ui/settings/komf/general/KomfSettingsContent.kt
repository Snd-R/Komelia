package io.github.snd_r.komelia.ui.settings.komf.general

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.unit.dp
import io.github.snd_r.komelia.ui.common.DropdownChoiceMenu
import io.github.snd_r.komelia.ui.common.DropdownMultiChoiceMenu
import io.github.snd_r.komelia.ui.common.LabeledEntry
import io.github.snd_r.komelia.ui.common.StateHolder
import io.github.snd_r.komelia.ui.common.SwitchWithLabel
import io.github.snd_r.komelia.ui.settings.komf.KomfMode
import io.github.snd_r.komelia.ui.settings.komf.SavableHttpTextField
import io.github.snd_r.komelia.ui.settings.komf.SavableTextField
import kotlinx.coroutines.launch
import snd.komga.client.library.KomgaLibrary
import snd.komga.client.library.KomgaLibraryId

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun KomfSettingsContent(
    komfEnabled: Boolean,
    onKomfEnabledChange: suspend (Boolean) -> Unit,
    komfMode: StateHolder<KomfMode>,
    komfUrl: StateHolder<String>,
    komfConnectionError: String?,

    komgaBaseUrl: StateHolder<String>,
    komgaUsername: StateHolder<String>,
    komgaPassword: StateHolder<String>,
    enableEventListener: StateHolder<Boolean>,
    metadataLibrariesFilter: List<KomgaLibraryId>,
    onMetadataLibraryFilterSelect: (KomgaLibraryId) -> Unit,
    notificationsFilter: List<KomgaLibraryId>,
    onNotificationsLibraryFilterSelect: (KomgaLibraryId) -> Unit,
    libraries: List<KomgaLibrary>,
    integrationToggleEnabled: Boolean
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        val uriHandler = LocalUriHandler.current
        val coroutineScope = rememberCoroutineScope()
        var komfEnabledConfirmed by remember { mutableStateOf(komfEnabled || !integrationToggleEnabled) }
        if (integrationToggleEnabled) {
            SwitchWithLabel(
                checked = komfEnabled,
                onCheckedChange = {
                    coroutineScope.launch {
                        onKomfEnabledChange(it)
                        komfEnabledConfirmed = true
                    }
                },
                label = { Text("Enable Komf Integration") },
                supportingText = {
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text("Adds features aimed at metadata updates and editing")
                        Spacer(Modifier.weight(1f))
                        ElevatedButton(
                            onClick = { uriHandler.openUri("https://github.com/Snd-R/komf") },
                            shape = RoundedCornerShape(5.dp),
                        ) {
                            Text("Project Link")
                        }
                    }
                }
            )
        }

        AnimatedVisibility(komfEnabled || !integrationToggleEnabled) {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                KomfConnectionDetails(komfMode, komfUrl, komfConnectionError, integrationToggleEnabled)

                AnimatedVisibility(komfConnectionError == null) {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        HorizontalDivider(Modifier.padding(vertical = 10.dp))
                        Text("Komga connection details", style = MaterialTheme.typography.titleLarge)
                        KomgaConnectionDetails(
                            komgaBaseUrl = komgaBaseUrl,
                            komgaUsername = komgaUsername,
                            komgaPassword = komgaPassword,
                            enableEventListener = enableEventListener,
                            metadataLibrariesFilter = metadataLibrariesFilter,
                            onMetadataLibraryFilterSelect = onMetadataLibraryFilterSelect,
                            notificationsFilter = notificationsFilter,
                            onNotificationsLibraryFilterSelect = onNotificationsLibraryFilterSelect,
                            libraries = libraries
                        )
                    }
                }

            }
        }
    }
}

@Composable
private fun KomfConnectionDetails(
    komfMode: StateHolder<KomfMode>,
    komfUrl: StateHolder<String>,
    komfConnectionError: String?,
    integrationToggleEnabled: Boolean
) {
    if (integrationToggleEnabled) {
        DropdownChoiceMenu(
            label = { Text("Mode") },
            selectedOption = remember { LabeledEntry(komfMode, "Remote server") },
            options = remember {
                listOf(
                    LabeledEntry(KomfMode.REMOTE, "Remote Server"),
                    LabeledEntry(KomfMode.EMBEDDED, "Embedded (Not Yet Implemented)"),
                )
            },
            onOptionChange = {},
            inputFieldModifier = Modifier.fillMaxWidth()
        )
    }
    if (komfMode.value == KomfMode.REMOTE) {
        SavableHttpTextField(
            label = "Komf Url",
            currentValue = komfUrl.value,
            onValueSave = { komfUrl.setValue(it) },
            confirmationText = "Connect",
            isError = komfConnectionError != null,
            supportingText = {
                if (komfConnectionError != null) {
                    Text(komfConnectionError)
                } else {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Connected", color = MaterialTheme.colorScheme.secondary)
                        Icon(Icons.Default.Check, null)
                    }

                }

            }
        )
    }

}

@Composable
private fun KomgaConnectionDetails(
    komgaBaseUrl: StateHolder<String>,
    komgaUsername: StateHolder<String>,
    komgaPassword: StateHolder<String>,

    enableEventListener: StateHolder<Boolean>,
    metadataLibrariesFilter: List<KomgaLibraryId>,
    onMetadataLibraryFilterSelect: (KomgaLibraryId) -> Unit,
    notificationsFilter: List<KomgaLibraryId>,
    onNotificationsLibraryFilterSelect: (KomgaLibraryId) -> Unit,
    libraries: List<KomgaLibrary>
) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {

        SavableHttpTextField(
            label = "Komga Url",
            confirmationText = "Save",
            currentValue = komgaBaseUrl.value,
            onValueSave = { komgaBaseUrl.setValue(it) },
            isError = komgaBaseUrl.errorMessage != null,
            supportingText = komgaBaseUrl.errorMessage?.let {
                { Text(text = it, color = MaterialTheme.colorScheme.error) }
            },
        )

        SavableTextField(
            currentValue = komgaUsername.value,
            onValueSave = { komgaUsername.setValue(it) },
            label = { Text("Komga Username") },
        )

        SavableTextField(
            currentValue = komgaPassword.value,
            onValueSave = { komgaPassword.setValue(it) },
            label = { Text("Komga Password") },
            useEditButton = true,
            isPassword = true
        )
    }

    Column {
        SwitchWithLabel(
            checked = enableEventListener.value,
            onCheckedChange = { enableEventListener.setValue(it) },
            label = { Text("Event Listener") },
            supportingText = {
                Text(
                    "Launch processing jobs when new series or book is added",
                    style = MaterialTheme.typography.labelLarge,
                )
            }
        )

        Spacer(Modifier.height(10.dp))

        AnimatedVisibility(enableEventListener.value) {
            EventListenerContent(
                metadataLibrariesFilter = metadataLibrariesFilter,
                onMetadataLibraryFilterSelect = onMetadataLibraryFilterSelect,
                notificationsFilter = notificationsFilter,
                onNotificationsLibraryFilterSelect = onNotificationsLibraryFilterSelect,
                libraries = libraries
            )
        }
    }

}

@Composable
private fun EventListenerContent(
    metadataLibrariesFilter: List<KomgaLibraryId>,
    onMetadataLibraryFilterSelect: (KomgaLibraryId) -> Unit,
    notificationsFilter: List<KomgaLibraryId>,
    onNotificationsLibraryFilterSelect: (KomgaLibraryId) -> Unit,
    libraries: List<KomgaLibrary>,
) {
    val libraryOptions = remember(libraries) { libraries.map { LabeledEntry(it.id, it.name) } }
    val metadataSelectedOptions = remember(metadataLibrariesFilter) {
        metadataLibrariesFilter.map { libraryId ->
            LabeledEntry(
                value = libraryId,
                label = libraries.find { it.id == libraryId }?.name
                    ?: "Unknown library: ${libraryId.value}"
            )
        }
    }
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        DropdownMultiChoiceMenu(
            selectedOptions = metadataSelectedOptions,
            options = libraryOptions,
            onOptionSelect = { onMetadataLibraryFilterSelect(it.value) },
            label = { Text("Enable metadata update jobs for libraries") },
            inputFieldModifier = Modifier.fillMaxWidth(),
            inputFieldColor = MaterialTheme.colorScheme.surfaceVariant
        )

        val notificationsSelectedOptions = remember(notificationsFilter) {
            notificationsFilter.map { libraryId ->
                LabeledEntry(
                    value = libraryId,
                    label = libraries.find { it.id == libraryId }?.name
                        ?: "Unknown library: id(${libraryId.value})"
                )
            }
        }
        DropdownMultiChoiceMenu(
            selectedOptions = notificationsSelectedOptions,
            options = libraryOptions,
            onOptionSelect = { onNotificationsLibraryFilterSelect(it.value) },
            label = { Text("Enable notification jobs for libraries") },
            inputFieldModifier = Modifier.fillMaxWidth(),
            inputFieldColor = MaterialTheme.colorScheme.surfaceVariant
        )
    }
}