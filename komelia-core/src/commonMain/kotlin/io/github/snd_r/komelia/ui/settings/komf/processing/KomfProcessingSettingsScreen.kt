package io.github.snd_r.komelia.ui.settings.komf.processing

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import cafe.adriel.voyager.core.model.rememberScreenModel
import cafe.adriel.voyager.core.screen.Screen
import io.github.snd_r.komelia.ui.LoadState
import io.github.snd_r.komelia.ui.LocalViewModelFactory
import io.github.snd_r.komelia.ui.common.LoadingMaxSizeIndicator
import io.github.snd_r.komelia.ui.settings.SettingsScreenContainer

class KomfProcessingSettingsScreen : Screen {

    @Composable
    override fun Content() {
        val viewModelFactory = LocalViewModelFactory.current
        val vm = rememberScreenModel { viewModelFactory.getKomfProcessingViewModel() }
        val vmState = vm.state.collectAsState().value
        val komfConfigLoadError = vm.komfConfig.errorFlow.collectAsState().value
        LaunchedEffect(Unit) { vm.initialize() }
        SettingsScreenContainer(title = "Metadata Processing Settings") {

            if (komfConfigLoadError != null) {
                Text("${komfConfigLoadError::class.simpleName}: ${komfConfigLoadError.message}")
                return@SettingsScreenContainer
            }

            when (vmState) {
                is LoadState.Error -> Text("${vmState.exception::class.simpleName}: ${vmState.exception.message}")
                LoadState.Loading, LoadState.Uninitialized -> LoadingMaxSizeIndicator()
                is LoadState.Success -> KomfProcessingSettingsContent(
                    defaultProcessingState = vm.defaultProcessingConfig,
                    libraryProcessingState = vm.libraryProcessingConfigs,
                    onLibraryConfigAdd = vm::onNewLibraryTabAdd,
                    onLibraryConfigRemove = vm::onLibraryTabRemove,
                    libraries = vm.libraries.collectAsState().value
                )
            }


        }
    }
}