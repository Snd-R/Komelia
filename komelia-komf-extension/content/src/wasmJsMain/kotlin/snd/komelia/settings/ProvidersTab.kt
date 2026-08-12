package snd.komelia.settings

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.FormatListBulleted
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.remember
import snd.komelia.LocalKomfViewModelFactory
import snd.komelia.ui.LoadState
import snd.komelia.ui.common.components.LoadingMaxSizeIndicator
import snd.komelia.ui.dialogs.tabs.DialogTab
import snd.komelia.ui.dialogs.tabs.TabItem
import snd.komelia.ui.error.formatExceptionMessage
import snd.komelia.ui.settings.komf.providers.KomfProvidersSettingsContent
import snd.komelia.ui.strings.AppStrings

class ProvidersTab : DialogTab {

    override fun options() = TabItem(
        title = AppStrings.komfProviders,
        icon = Icons.AutoMirrored.Filled.FormatListBulleted
    )

    @Composable
    override fun Content() {
        val viewModelFactory = LocalKomfViewModelFactory.current
        val vm = remember { viewModelFactory.getKomfProvidersViewModel() }
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
            is LoadState.Success -> KomfProvidersSettingsContent(
                defaultProcessingState = vm.defaultProvidersConfig,
                libraryProcessingState = vm.libraryProvidersConfigs,
                onLibraryConfigAdd = vm::onNewLibraryTabAdd,
                onLibraryConfigRemove = vm::onLibraryTabRemove,
                libraries = vm.libraries.collectAsState(emptyList()).value,
                nameMatchingMode = vm.nameMatchingMode,
                onNameMatchingModeChange = vm::onNameMatchingModeChange,
                comicVineClientId = vm.comicVineClientId,
                onComicVineClientIdSave = vm::onComicVineClientIdChange,
                malClientId = vm.malClientId,
                onMalClientIdSave = vm::onMalClientIdChange,
                mangaBakaDbMetadata = vm.mangaBakaDbMetadata,
                onMangaBakaUpdate = vm::onMangaBakaDbUpdate
            )
        }

    }
}
