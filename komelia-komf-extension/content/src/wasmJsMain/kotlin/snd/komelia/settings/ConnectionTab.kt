package snd.komelia.settings

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.SettingsEthernet
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.remember
import snd.komelia.LocalKomfViewModelFactory
import snd.komelia.ui.LoadState
import snd.komelia.ui.common.components.LoadingMaxSizeIndicator
import snd.komelia.ui.dialogs.tabs.DialogTab
import snd.komelia.ui.dialogs.tabs.TabItem
import snd.komelia.ui.settings.komf.general.KomfSettingsContent
import snd.komelia.ui.strings.AppStrings
import snd.komf.api.MediaServer

class ConnectionTab(private val mediaServer: MediaServer) : DialogTab {

    override fun options() = TabItem(
        title = AppStrings.komfConnection,
        icon = Icons.Default.SettingsEthernet
    )

    @Composable
    override fun Content() {
        val viewModelFactory = LocalKomfViewModelFactory.current
        val vm = remember { viewModelFactory.getKomfSettingsViewModel(mediaServer = mediaServer) }
        val vmState = vm.state.collectAsState().value
        LaunchedEffect(Unit) { vm.initialize() }

        when (vmState) {
            LoadState.Loading, LoadState.Uninitialized -> LoadingMaxSizeIndicator()
            is LoadState.Error, is LoadState.Success -> KomfSettingsContent(
                komfEnabled = vm.komfEnabled.collectAsState().value,
                onKomfEnabledChange = vm::onKomfEnabledChange,
                komfUrl = vm.komfUrl.collectAsState().value,
                onKomfUrlChange = vm::onKomfUrlChange,
                komfConnectionError = vm.komfConnectionError.collectAsState().value,
                integrationToggleEnabled = false,
                komgaState = if (mediaServer == MediaServer.KOMGA) vm.komgaConnectionState else null,
                kavitaState = if (mediaServer == MediaServer.KAVITA) vm.kavitaConnectionState else null,
            )
        }
    }
}
