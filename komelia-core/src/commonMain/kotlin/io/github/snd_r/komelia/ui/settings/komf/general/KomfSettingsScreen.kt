package io.github.snd_r.komelia.ui.settings.komf.general

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import cafe.adriel.voyager.core.model.rememberScreenModel
import cafe.adriel.voyager.core.screen.Screen
import io.github.snd_r.komelia.ui.LoadState
import io.github.snd_r.komelia.ui.LocalViewModelFactory
import io.github.snd_r.komelia.ui.common.LoadingMaxSizeIndicator
import io.github.snd_r.komelia.ui.common.StateHolder
import io.github.snd_r.komelia.ui.settings.SettingsScreenContainer

class KomfSettingsScreen(private val integrationToggleEnabled: Boolean = true) : Screen {

    @Composable
    override fun Content() {
        val viewModelFactory = LocalViewModelFactory.current
        val vm = rememberScreenModel { viewModelFactory.getKomfSettingsViewModel() }
        val vmState = vm.state.collectAsState().value
        LaunchedEffect(Unit) { vm.initialize() }
        SettingsScreenContainer(title = "Komf Settings") {
            when (vmState) {

                LoadState.Loading, LoadState.Uninitialized -> LoadingMaxSizeIndicator()
                is LoadState.Error, is LoadState.Success -> KomfSettingsContent(
                    komfEnabled = vm.komfEnabled,
                    onKomfEnabledChange = vm::onKomfEnabledChange,
                    komfMode = StateHolder(vm.komfMode, {}),
                    komfUrl = StateHolder(vm.komfUrl, vm::onKomfUrlChange),
                    komfConnectionError = vm.komfConnectionError,

                    komgaBaseUrl = StateHolder(vm.komgaBaseUrl, vm::onKomgaBaseUrlChange),
                    komgaUsername = StateHolder(vm.komgaUsername, vm::onKomgaUsernameChange),
                    komgaPassword = StateHolder("", vm::onKomgaPasswordUpdate),
                    enableEventListener = StateHolder(vm.enableEventListener, vm::onEventListenerEnable),
                    metadataLibrariesFilter = vm.metadataLibraryFilters,
                    onMetadataLibraryFilterSelect = vm::onMetadataLibraryFilterSelect,
                    notificationsFilter = vm.notificationsLibraryFilters,
                    onNotificationsLibraryFilterSelect = vm::onNotificationsLibraryFilterSelect,
                    libraries = vm.libraries.collectAsState().value,
                    integrationToggleEnabled = integrationToggleEnabled
                )
            }

        }
    }
}