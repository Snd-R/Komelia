package io.github.snd_r.komelia.ui.settings.komf.general

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import cafe.adriel.voyager.core.model.rememberScreenModel
import cafe.adriel.voyager.core.screen.Screen
import io.github.snd_r.komelia.ui.LoadState
import io.github.snd_r.komelia.ui.LocalViewModelFactory
import io.github.snd_r.komelia.ui.common.LoadingMaxSizeIndicator
import io.github.snd_r.komelia.ui.settings.SettingsScreenContainer

class KomfSettingsScreen(
    private val integrationToggleEnabled: Boolean = true,
    private val showKavitaSettings: Boolean = false,
) : Screen {

    @Composable
    override fun Content() {
        val viewModelFactory = LocalViewModelFactory.current
        val vm = rememberScreenModel("${integrationToggleEnabled}_${showKavitaSettings}") {
            viewModelFactory.getKomfSettingsViewModel(
                enableKavita = showKavitaSettings,
                integrationToggleEnabled = integrationToggleEnabled
            )
        }
        val vmState = vm.state.collectAsState().value
        LaunchedEffect(Unit) { vm.initialize() }

        val komfConnectionError = vm.komfConnectionError.collectAsState().value
        SettingsScreenContainer(title = "Komf Settings") {
            when (vmState) {

                LoadState.Loading, LoadState.Uninitialized -> LoadingMaxSizeIndicator()
                is LoadState.Error, is LoadState.Success -> KomfSettingsContent(
                    komfEnabled = vm.komfEnabled.collectAsState().value,
                    onKomfEnabledChange = vm::onKomfEnabledChange,
                    komfUrl = vm.komfUrl.collectAsState().value,
                    onKomfUrlChange = vm::onKomfUrlChange,
                    komfConnectionError = komfConnectionError,
                    integrationToggleEnabled = integrationToggleEnabled,
                    komgaState = vm.komgaConnectionState,
                    kavitaState = vm.kavitaConnectionState,
                )
            }

        }
    }
}