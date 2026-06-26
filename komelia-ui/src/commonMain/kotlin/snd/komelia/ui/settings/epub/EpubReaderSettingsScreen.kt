package snd.komelia.ui.settings.epub

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import cafe.adriel.voyager.core.model.rememberScreenModel
import cafe.adriel.voyager.core.screen.Screen
import snd.komelia.ui.LoadState
import snd.komelia.ui.LocalViewModelFactory
import snd.komelia.ui.common.components.ErrorContent
import snd.komelia.ui.common.components.LoadingMaxSizeIndicator
import snd.komelia.ui.settings.SettingsScreenContainer

class EpubReaderSettingsScreen : Screen {

    @Composable
    override fun Content() {
        val viewModelFactory = LocalViewModelFactory.current
        val vm = rememberScreenModel { viewModelFactory.getEpubReaderSettingsViewModel() }
        LaunchedEffect(Unit) {
            vm.initialize()
        }
        SettingsScreenContainer(title = "Epub Reader Settings") {
            when (val result = vm.state.collectAsState().value) {
                is LoadState.Error -> ErrorContent(result.exception)
                LoadState.Uninitialized, LoadState.Loading -> LoadingMaxSizeIndicator()
                is LoadState.Success<Unit> -> EpubReaderSettingsContent(
                    readerType = vm.selectedEpubReader.collectAsState().value,
                    onReaderChange = vm::onSelectedTypeChange,
                    fullscreenEnabled = vm.fullscreenEnabled.collectAsState().value,
                    onFullscreenEnabledChange = vm::onFullscreenEnabledChange,
                )
            }

        }
    }
}