package snd.komelia.settings

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.remember
import io.github.snd_r.komelia.ui.LoadState
import io.github.snd_r.komelia.ui.common.LoadingMaxSizeIndicator
import io.github.snd_r.komelia.ui.dialogs.tabs.DialogTab
import io.github.snd_r.komelia.ui.dialogs.tabs.TabItem
import io.github.snd_r.komelia.ui.error.formatExceptionMessage
import io.github.snd_r.komelia.ui.settings.komf.processing.KomfProcessingSettingsContent
import snd.komelia.LocalKomfViewModelFactory
import snd.komf.api.MediaServer

class ProcessingTab : DialogTab {

    override fun options() = TabItem(
        title = "Processing",
        icon = Icons.Default.Memory
    )

    @Composable
    override fun Content() {
        val viewModelFactory = LocalKomfViewModelFactory.current
        val vm = remember { viewModelFactory.getKomfProcessingViewModel(MediaServer.KOMGA) }
        val vmState = vm.state.collectAsState().value
        val komfConfigLoadError = vm.komfSharedState.configError.collectAsState().value
        LaunchedEffect(Unit) { vm.initialize() }

        if (komfConfigLoadError != null) {
            Text(formatExceptionMessage(komfConfigLoadError))
            return
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
                serverType = MediaServer.KOMGA,
            )
        }

    }
}
