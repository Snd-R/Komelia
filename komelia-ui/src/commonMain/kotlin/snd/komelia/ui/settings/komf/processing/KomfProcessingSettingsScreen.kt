package snd.komelia.ui.settings.komf.processing

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.remember
import cafe.adriel.voyager.core.model.rememberScreenModel
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.core.screen.ScreenKey
import snd.komelia.ui.LoadState
import snd.komelia.ui.LocalViewModelFactory
import snd.komelia.ui.common.components.LoadingMaxSizeIndicator
import snd.komelia.ui.error.formatExceptionMessage
import snd.komelia.ui.settings.SettingsScreenContainer
import snd.komf.api.MediaServer
import snd.komf.api.MediaServer.KAVITA
import snd.komf.api.MediaServer.KOMGA

class KomfProcessingSettingsScreen(val serverType: MediaServer) : Screen {
    override val key: ScreenKey = serverType.name

    @Composable
    override fun Content() {
        val viewModelFactory = LocalViewModelFactory.current
        val vm = rememberScreenModel(serverType.name) { viewModelFactory.getKomfProcessingViewModel(serverType) }
        val vmState = vm.state.collectAsState().value
        val komfConfigLoadError = vm.komfSharedState.configError.collectAsState().value
        LaunchedEffect(Unit) { vm.initialize() }
        val title = remember(serverType.name) {
            val serverName = when (serverType) {
                KOMGA -> "Komga"
                KAVITA -> "Kavita"
            }

            "$serverName Metadata Processing Settings"
        }
        SettingsScreenContainer(title = title) {

            if (komfConfigLoadError != null) {
                Text(formatExceptionMessage(komfConfigLoadError))
                return@SettingsScreenContainer
            }

            when (vmState) {
                is LoadState.Error -> Text(formatExceptionMessage(vmState.exception))
                LoadState.Loading, LoadState.Uninitialized -> LoadingMaxSizeIndicator()
                is LoadState.Success -> KomfProcessingSettingsContent(
                    defaultProcessingState = vm.defaultProcessingConfig.collectAsState().value,
                    libraryProcessingState = vm.libraryProcessingConfigs.collectAsState().value,
                    onLibraryConfigAdd = vm::onNewLibraryTabAdd,
                    onLibraryConfigRemove = vm::onLibraryTabRemove,
                    libraries = vm.libraries.collectAsState(emptyList()).value,
                    serverType = serverType,
                )
            }


        }
    }
}