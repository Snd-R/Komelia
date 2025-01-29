package io.github.snd_r.komelia.ui.settings.komf.general

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.unit.dp
import io.github.snd_r.komelia.platform.cursorForHand
import io.github.snd_r.komelia.ui.common.DropdownMultiChoiceMenu
import io.github.snd_r.komelia.ui.common.LabeledEntry
import io.github.snd_r.komelia.ui.common.SwitchWithLabel
import io.github.snd_r.komelia.ui.settings.komf.SavableHttpTextField
import io.github.snd_r.komelia.ui.settings.komf.SavableTextField
import kotlinx.coroutines.launch
import snd.komf.api.mediaserver.KomfMediaServerLibrary
import snd.komf.api.mediaserver.KomfMediaServerLibraryId

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun KomfSettingsContent(
    komfEnabled: Boolean,
    onKomfEnabledChange: suspend (Boolean) -> Unit,
    komfUrl: String,
    onKomfUrlChange: (String) -> Unit,
    integrationToggleEnabled: Boolean,
    komfConnectionError: String?,
    komgaState: KomgaConnectionState?,
    kavitaState: KavitaConnectionState?,
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        val uriHandler = LocalUriHandler.current
        val coroutineScope = rememberCoroutineScope()
        var komfEnabledConfirmed by remember { mutableStateOf(komfEnabled || !integrationToggleEnabled) }
        if (integrationToggleEnabled) {
            Column {
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
                        Text("Adds features aimed at metadata updates and editing")
                    }
                )

                Row {
                    Spacer(Modifier.weight(1f))
                    ElevatedButton(
                        onClick = { uriHandler.openUri("https://github.com/Snd-R/komf") },
                        shape = RoundedCornerShape(5.dp),
                    ) {
                        Text("Project Link")
                    }
                }
            }
        }

        AnimatedVisibility(komfEnabled || !integrationToggleEnabled) {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                KomfConnectionDetails(
                    komfUrl = komfUrl,
                    onKomfUrlChange = onKomfUrlChange,
                    komfConnectionError = komfConnectionError
                )

                AnimatedVisibility(komfConnectionError == null) {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        HorizontalDivider(Modifier.padding(vertical = 10.dp))
                        when {
                            komgaState != null && kavitaState != null -> KomgaAndKavitaConnectionSettings(
                                komgaState = komgaState,
                                kavitaState = kavitaState
                            )

                            komgaState != null -> KomgaConnectionDetails(komgaState)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun KomgaAndKavitaConnectionSettings(
    komgaState: KomgaConnectionState,
    kavitaState: KavitaConnectionState,
) {
    var selectedTab by remember { mutableStateOf(0) }
    TabRow(selectedTabIndex = selectedTab) {
        Tab(
            selected = selectedTab == 0,
            onClick = { selectedTab = 0 },
            modifier = Modifier.heightIn(min = 40.dp).cursorForHand(),
        ) {
            Text("Komga")
        }
        Tab(
            selected = selectedTab == 1,
            onClick = { selectedTab = 1 },
            modifier = Modifier.heightIn(min = 40.dp).cursorForHand(),
        ) {
            Text("Kavita")
        }
    }

    when (selectedTab) {
        0 -> KomgaConnectionDetails(komgaState)
        1 -> KavitaConnectionDetails(kavitaState)
    }

}

@Composable
private fun KomfConnectionDetails(
    komfUrl: String,
    onKomfUrlChange: (String) -> Unit,
    komfConnectionError: String?,
) {
    SavableHttpTextField(
        label = "Komf Url",
        currentValue = komfUrl,
        onValueSave = onKomfUrlChange,
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

@Composable
private fun KomgaConnectionDetails(
    state: KomgaConnectionState,
) {
    val baseUrl = state.baseUrl.collectAsState().value
    val onBaseUrlChange = state::onKomgaBaseUrlChange
    val username = state.username.collectAsState().value
    val onUsernameChange = state::onKomgaUsernameChange
    val onPasswordChange = state::onKomgaPasswordUpdate
    val connectionError = state.connectionError.collectAsState().value
    val enableEventListener = state.enableEventListener.collectAsState().value
    val onEnableEventListenerChange = state::onEventListenerEnable
    val metadataLibrariesFilter = state.metadataLibraryFilters.collectAsState().value
    val onMetadataLibraryFilterSelect = state::onMetadataLibraryFilterSelect
    val notificationsFilter = state.notificationsLibraryFilters.collectAsState().value
    val onNotificationsLibraryFilterSelect = state::onNotificationsLibraryFilterSelect
    val libraries = state.libraries.collectAsState(emptyList()).value

    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        if (connectionError != null) {
            Text(connectionError, color = MaterialTheme.colorScheme.error)
        }

        SavableHttpTextField(
            label = "Komga Url",
            confirmationText = "Save",
            currentValue = baseUrl,
            onValueSave = onBaseUrlChange,
        )

        SavableTextField(
            currentValue = username,
            onValueSave = onUsernameChange,
            label = { Text("Komga Username") },
        )

        SavableTextField(
            currentValue = "",
            onValueSave = onPasswordChange,
            label = { Text("Komga Password") },
            useEditButton = true,
            isPassword = true
        )
    }
    MediaServerEventListenerSettings(
        enableEventListener = enableEventListener,
        onEnableEventListenerChange = onEnableEventListenerChange,
        metadataLibrariesFilter = metadataLibrariesFilter,
        onMetadataLibraryFilterSelect = onMetadataLibraryFilterSelect,
        notificationsFilter = notificationsFilter,
        onNotificationsLibraryFilterSelect = onNotificationsLibraryFilterSelect,
        libraries = libraries
    )
}

@Composable
private fun KavitaConnectionDetails(
    state: KavitaConnectionState,
) {
    val baseUrl = state.baseUrl.collectAsState().value
    val onBaseUrlChange = state::onBaseUrlChange
    val onPasswordChange = state::onApiKeyUpdate
    val connectionError = state.connectionError.collectAsState().value
    val enableEventListener = state.enableEventListener.collectAsState().value
    val onEnableEventListenerChange = state::onEventListenerEnable
    val metadataLibrariesFilter = state.metadataLibraryFilters.collectAsState().value
    val onMetadataLibraryFilterSelect = state::onMetadataLibraryFilterSelect
    val notificationsFilter = state.notificationsLibraryFilters.collectAsState().value
    val onNotificationsLibraryFilterSelect = state::onNotificationsLibraryFilterSelect
    val libraries = state.libraries.collectAsState(emptyList()).value

    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        if (connectionError != null) {
            Text(connectionError, color = MaterialTheme.colorScheme.error)
        }

        SavableHttpTextField(
            label = "Kavita Url",
            confirmationText = "Save",
            currentValue = baseUrl,
            onValueSave = onBaseUrlChange,
        )

        SavableTextField(
            currentValue = "",
            onValueSave = onPasswordChange,
            label = { Text("Kavita API Key") },
            useEditButton = true,
            isPassword = true
        )
    }

    MediaServerEventListenerSettings(
        enableEventListener = enableEventListener,
        onEnableEventListenerChange = onEnableEventListenerChange,
        metadataLibrariesFilter = metadataLibrariesFilter,
        onMetadataLibraryFilterSelect = onMetadataLibraryFilterSelect,
        notificationsFilter = notificationsFilter,
        onNotificationsLibraryFilterSelect = onNotificationsLibraryFilterSelect,
        libraries = libraries
    )


}

@Composable
private fun MediaServerEventListenerSettings(
    enableEventListener: Boolean,
    onEnableEventListenerChange: (Boolean) -> Unit,
    metadataLibrariesFilter: List<KomfMediaServerLibraryId>,
    onMetadataLibraryFilterSelect: (KomfMediaServerLibraryId) -> Unit,
    notificationsFilter: List<KomfMediaServerLibraryId>,
    onNotificationsLibraryFilterSelect: (KomfMediaServerLibraryId) -> Unit,
    libraries: List<KomfMediaServerLibrary>
) {
    Column {
        SwitchWithLabel(
            checked = enableEventListener,
            onCheckedChange = onEnableEventListenerChange,
            label = { Text("Event Listener") },
            supportingText = {
                Text(
                    "Launch processing jobs when new series or book is added",
                    style = MaterialTheme.typography.labelLarge,
                )
            }
        )

        Spacer(Modifier.height(10.dp))

        AnimatedVisibility(enableEventListener) {
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
    metadataLibrariesFilter: List<KomfMediaServerLibraryId>,
    onMetadataLibraryFilterSelect: (KomfMediaServerLibraryId) -> Unit,
    notificationsFilter: List<KomfMediaServerLibraryId>,
    onNotificationsLibraryFilterSelect: (KomfMediaServerLibraryId) -> Unit,
    libraries: List<KomfMediaServerLibrary>,
) {
    val libraryOptions = remember(libraries) {
        val ids = libraries.map { it.id }
        val unknown = metadataLibrariesFilter.filter { it !in ids }
            .map { LabeledEntry(it, "Unknown library: ${it.value}") }
        libraries.map { LabeledEntry(it.id, it.name) }.plus(unknown)
    }
    val metadataSelectedOptions = remember(metadataLibrariesFilter, libraries) {
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

        val notificationsSelectedOptions = remember(notificationsFilter, libraries) {
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