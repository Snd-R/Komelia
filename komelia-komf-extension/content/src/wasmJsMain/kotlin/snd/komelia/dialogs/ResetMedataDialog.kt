package snd.komelia.dialogs

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import snd.komelia.LocalKomfViewModelFactory
import snd.komelia.ui.dialogs.komf.reset.KomfResetLibraryMetadataDialog
import snd.komelia.ui.dialogs.komf.reset.KomfResetSeriesMetadataDialog
import snd.komf.api.KomfServerLibraryId
import snd.komf.api.KomfServerSeriesId
import snd.komf.api.MediaServer

@Composable
fun ResetSeriesMetadataDialog(
    mediaServer: MediaServer,
    seriesId: KomfServerSeriesId,
    libraryId: KomfServerLibraryId,
    onDismissRequest: () -> Unit,
) {
    val viewModelFactory = LocalKomfViewModelFactory.current
    val vm = remember { viewModelFactory.getKomfResetMetadataDialogViewModel(onDismissRequest, mediaServer) }
    KomfResetSeriesMetadataDialog(
        removeComicInfo = vm.removeComicInfo,
        onRemoveComicInfoChange = vm::removeComicInfo::set,
        onConfirm = { vm.onSeriesReset(seriesId, libraryId) },
        onDismissRequest = onDismissRequest
    )
}

@Composable
fun ResetLibraryMetadataDialog(
    mediaServer: MediaServer,
    libraryId: KomfServerLibraryId,
    onDismissRequest: () -> Unit,
) {
    val viewModelFactory = LocalKomfViewModelFactory.current
    val vm = remember { viewModelFactory.getKomfResetMetadataDialogViewModel(onDismissRequest, mediaServer) }
    KomfResetLibraryMetadataDialog(
        removeComicInfo = vm.removeComicInfo,
        onRemoveComicInfoChange = vm::removeComicInfo::set,
        onConfirm = { vm.onLibraryReset(libraryId) },
        onDismissRequest = onDismissRequest
    )
}
