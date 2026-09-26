package snd.komelia.ui.settings.komf.mangabaka

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import cafe.adriel.voyager.core.model.rememberScreenModel
import cafe.adriel.voyager.core.screen.Screen
import snd.komelia.ui.LoadState
import snd.komelia.ui.LocalViewModelFactory
import snd.komelia.ui.common.components.LoadingMaxSizeIndicator
import snd.komelia.ui.settings.SettingsScreenContainer

class KomfMangaBakaSettingsScreen : Screen {

    @Composable
    override fun Content() {
        val viewModelFactory = LocalViewModelFactory.current
        val vm = rememberScreenModel { viewModelFactory.getKomfMangaBakaSettingsViewModel() }
        val vmState = vm.state.collectAsState().value
        LaunchedEffect(Unit) { vm.initialize() }
        SettingsScreenContainer(title = "MangaBaka Settings") {
            when (vmState) {
                LoadState.Loading, LoadState.Uninitialized -> LoadingMaxSizeIndicator()
                else -> KomfMangaBakaSettingsContent(
                    enabled = vm.enabled.collectAsState().value,
                    onEnabledChange = vm::onEnabledChange,
                    mangaBakaDbMetadata = vm.mangaBakaDbInfo,
                    onMangaBakaUpdate = vm::onMangaBakaDbUpdate,
                    error = vm.errorMessage.collectAsState().value
                )
            }
        }
    }
}
