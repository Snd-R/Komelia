package snd.komelia.ui.settings.imagereader

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import cafe.adriel.voyager.core.model.rememberScreenModel
import cafe.adriel.voyager.core.screen.Screen
import io.github.snd_r.komelia.ui.komelia_ui.generated.resources.Res
import io.github.snd_r.komelia.ui.komelia_ui.generated.resources.settings_image_title
import org.jetbrains.compose.resources.stringResource
import snd.komelia.ui.LocalViewModelFactory
import snd.komelia.ui.settings.SettingsScreenContainer

class ImageReaderSettingsScreen : Screen {

    @Composable
    override fun Content() {
        val viewModelFactory = LocalViewModelFactory.current
        val vm = rememberScreenModel { viewModelFactory.getImageReaderSettingsViewModel() }
        LaunchedEffect(Unit) { vm.initialize() }

        SettingsScreenContainer(stringResource(Res.string.settings_image_title)) {
            ImageReaderSettingsContent(
                loadThumbnailPreviews = vm.loadThumbnailsPreview.collectAsState().value,
                onLoadThumbnailPreviewsChange = vm::onLoadThumbnailsPreviewChange,
                volumeKeysNavigation = vm.volumeKeysNavigation.collectAsState().value,
                onVolumeKeysNavigationChange = vm::onVolumeKeysNavigationChange,

                onCacheClear = vm::onClearImageCache,
                onnxRuntimeSettingsState = vm.onnxRuntimeSettingsState,
            )
        }
    }
}
