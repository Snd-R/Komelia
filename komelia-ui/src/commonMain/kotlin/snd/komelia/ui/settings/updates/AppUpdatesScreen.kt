package snd.komelia.ui.settings.updates

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import cafe.adriel.voyager.core.model.rememberScreenModel
import cafe.adriel.voyager.core.screen.Screen
import snd.komelia.ui.LoadState
import snd.komelia.ui.LocalViewModelFactory
import snd.komelia.ui.settings.SettingsScreenContainer
import snd.komelia.updates.AppVersion

class AppUpdatesScreen : Screen {
    @Composable
    override fun Content() {
        val viewModelFactory = LocalViewModelFactory.current
        val vm = rememberScreenModel { viewModelFactory.getSettingsUpdatesViewModel() }
        LaunchedEffect(Unit) { vm.initialize() }

        val state = vm.state.collectAsState().value
        SettingsScreenContainer("App Updates") {
            when (state) {
                is LoadState.Error -> Text("Error ${state.exception.message}")
                LoadState.Loading, LoadState.Uninitialized, is LoadState.Success -> AppUpdatesContent(
                    checkForUpdates = vm.checkForUpdatesOnStartup.collectAsState().value,
                    onCheckForUpdatesChange = vm::onCheckForUpdatesOnStartupChange,
                    currentVersion = AppVersion.current,
                    releases = vm.releases.collectAsState().value,

                    latestVersion = vm.latestVersion.collectAsState().value,
                    lastChecked = vm.lastUpdateCheck.collectAsState().value,
                    onCheckForUpdates = vm::checkForUpdates,
                    versionCheckInProgress = state == LoadState.Loading,
                    onUpdate = vm::onUpdate,
                    onUpdateCancel = vm::onUpdateCancel,
                    downloadProgress = vm.downloadProgress.collectAsState().value
                )
            }

        }
    }
}