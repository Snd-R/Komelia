package snd.komelia.ui.settings.komf.providers

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import cafe.adriel.voyager.core.model.rememberScreenModel
import cafe.adriel.voyager.core.screen.Screen
import io.github.snd_r.komelia.ui.komelia_ui.generated.resources.Res
import io.github.snd_r.komelia.ui.komelia_ui.generated.resources.komf_settings_providers_title
import org.jetbrains.compose.resources.stringResource
import snd.komelia.ui.LoadState
import snd.komelia.ui.LocalViewModelFactory
import snd.komelia.ui.common.components.LoadingMaxSizeIndicator
import snd.komelia.ui.error.formatExceptionMessage
import snd.komelia.ui.settings.SettingsScreenContainer

class KomfProvidersSettingsScreen : Screen {

    @Composable
    override fun Content() {
        val viewModelFactory = LocalViewModelFactory.current
        val vm = rememberScreenModel { viewModelFactory.getKomfProvidersViewModel() }
        val vmState = vm.state.collectAsState().value
        val komfConfigLoadError = vm.komfSharedState.configError.collectAsState().value
        LaunchedEffect(Unit) { vm.initialize() }
        SettingsScreenContainer(title = stringResource(Res.string.komf_settings_providers_title)) {

            if (komfConfigLoadError != null) {
                Text(formatExceptionMessage(komfConfigLoadError))
                return@SettingsScreenContainer
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
}