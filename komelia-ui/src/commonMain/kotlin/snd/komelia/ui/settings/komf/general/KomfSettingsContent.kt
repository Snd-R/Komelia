package snd.komelia.ui.settings.komf.general

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SecondaryTabRow
import androidx.compose.material3.Tab
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
import io.github.snd_r.komelia.ui.komelia_ui.generated.resources.Res
import io.github.snd_r.komelia.ui.komelia_ui.generated.resources.komf_connection_connect
import io.github.snd_r.komelia.ui.komelia_ui.generated.resources.komf_connection_connected
import io.github.snd_r.komelia.ui.komelia_ui.generated.resources.komf_connection_kavita
import io.github.snd_r.komelia.ui.komelia_ui.generated.resources.komf_connection_kavita_api_key
import io.github.snd_r.komelia.ui.komelia_ui.generated.resources.komf_connection_kavita_url
import io.github.snd_r.komelia.ui.komelia_ui.generated.resources.komf_connection_komga
import io.github.snd_r.komelia.ui.komelia_ui.generated.resources.komf_connection_komga_password
import io.github.snd_r.komelia.ui.komelia_ui.generated.resources.komf_connection_komga_url
import io.github.snd_r.komelia.ui.komelia_ui.generated.resources.komf_connection_komga_username
import io.github.snd_r.komelia.ui.komelia_ui.generated.resources.komf_connection_save
import io.github.snd_r.komelia.ui.komelia_ui.generated.resources.komf_connection_url
import io.github.snd_r.komelia.ui.komelia_ui.generated.resources.komf_enable
import io.github.snd_r.komelia.ui.komelia_ui.generated.resources.komf_enable_desc
import io.github.snd_r.komelia.ui.komelia_ui.generated.resources.komf_event_listener
import io.github.snd_r.komelia.ui.komelia_ui.generated.resources.komf_event_listener_desc
import io.github.snd_r.komelia.ui.komelia_ui.generated.resources.komf_event_listener_libraries
import io.github.snd_r.komelia.ui.komelia_ui.generated.resources.komf_event_listener_notifications
import io.github.snd_r.komelia.ui.komelia_ui.generated.resources.komf_github
import io.github.snd_r.komelia.ui.komelia_ui.generated.resources.komf_unknown_library
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.stringResource
import snd.komelia.ui.common.components.DropdownMultiChoiceMenu
import snd.komelia.ui.common.components.LabeledEntry
import snd.komelia.ui.common.components.SwitchWithLabel
import snd.komelia.ui.platform.cursorForHand
import snd.komelia.ui.settings.komf.SavableHttpTextField
import snd.komelia.ui.settings.komf.SavableTextField
import snd.komf.api.mediaserver.KomfMediaServerLibrary
import snd.komf.api.mediaserver.KomfMediaServerLibraryId

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
                    label = { Text(stringResource(Res.string.komf_enable)) },
                    supportingText = {
                        Text(stringResource(Res.string.komf_enable_desc))
                    }
                )

                Row {
                    Spacer(Modifier.weight(1f))
                    ElevatedButton(
                        onClick = { uriHandler.openUri("https://github.com/Snd-R/komf") },
                    ) {
                        Text(stringResource(Res.string.komf_github))
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
                            kavitaState != null -> KavitaConnectionDetails(kavitaState)
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
    SecondaryTabRow(selectedTabIndex = selectedTab) {
        Tab(
            selected = selectedTab == 0,
            onClick = { selectedTab = 0 },
            modifier = Modifier.heightIn(min = 40.dp).cursorForHand(),
        ) {
            Text(stringResource(Res.string.komf_connection_komga))
        }
        Tab(
            selected = selectedTab == 1,
            onClick = { selectedTab = 1 },
            modifier = Modifier.heightIn(min = 40.dp).cursorForHand(),
        ) {
            Text(stringResource(Res.string.komf_connection_kavita))
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
        label = stringResource(Res.string.komf_connection_url),
        currentValue = komfUrl,
        onValueSave = onKomfUrlChange,
        confirmationText = stringResource(Res.string.komf_connection_connect),
        isError = komfConnectionError != null,
        supportingText = {
            if (komfConnectionError != null) {
                Text(komfConnectionError)
            } else {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        stringResource(Res.string.komf_connection_connected),
                        color = MaterialTheme.colorScheme.secondary
                    )
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
            label = stringResource(Res.string.komf_connection_komga_url),
            confirmationText = stringResource(Res.string.komf_connection_save),
            currentValue = baseUrl,
            onValueSave = onBaseUrlChange,
        )

        SavableTextField(
            currentValue = username,
            onValueSave = onUsernameChange,
            label = { Text(stringResource(Res.string.komf_connection_komga_username)) },
        )

        SavableTextField(
            currentValue = "",
            onValueSave = onPasswordChange,
            label = { Text(stringResource(Res.string.komf_connection_komga_password)) },
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
            label = stringResource(Res.string.komf_connection_kavita_url),
            confirmationText = stringResource(Res.string.komf_connection_save),
            currentValue = baseUrl,
            onValueSave = onBaseUrlChange,
        )

        SavableTextField(
            currentValue = "",
            onValueSave = onPasswordChange,
            label = { Text(stringResource(Res.string.komf_connection_kavita_api_key)) },
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
            label = { Text(stringResource(Res.string.komf_event_listener)) },
            supportingText = {
                Text(
                    stringResource(Res.string.komf_event_listener_desc),
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
    val unknownLibraryString = stringResource(Res.string.komf_unknown_library)
    val libraryOptions = remember(libraries) {
        val ids = libraries.map { it.id }
        val unknown = metadataLibrariesFilter.filter { it !in ids }
            .map { LabeledEntry(it, "$unknownLibraryString ${it.value}") }
        libraries.map { LabeledEntry(it.id, it.name) }.plus(unknown)
    }
    val metadataSelectedOptions = remember(metadataLibrariesFilter, libraries) {
        metadataLibrariesFilter.map { libraryId ->
            LabeledEntry(
                value = libraryId,
                label = libraries.find { it.id == libraryId }?.name
                    ?: "$unknownLibraryString ${libraryId.value}"
            )
        }
    }
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        DropdownMultiChoiceMenu(
            selectedOptions = metadataSelectedOptions,
            options = libraryOptions,
            onOptionSelect = { onMetadataLibraryFilterSelect(it.value) },
            label = { Text(stringResource(Res.string.komf_event_listener_libraries)) },
            inputFieldModifier = Modifier.fillMaxWidth(),
            inputFieldColor = MaterialTheme.colorScheme.surfaceVariant
        )

        val notificationsSelectedOptions = remember(notificationsFilter, libraries) {
            notificationsFilter.map { libraryId ->
                LabeledEntry(
                    value = libraryId,
                    label = libraries.find { it.id == libraryId }?.name
                        ?: "$unknownLibraryString: id(${libraryId.value})"
                )
            }
        }
        DropdownMultiChoiceMenu(
            selectedOptions = notificationsSelectedOptions,
            options = libraryOptions,
            onOptionSelect = { onNotificationsLibraryFilterSelect(it.value) },
            label = { Text(stringResource(Res.string.komf_event_listener_notifications)) },
            inputFieldModifier = Modifier.fillMaxWidth(),
            inputFieldColor = MaterialTheme.colorScheme.surfaceVariant
        )
    }
}