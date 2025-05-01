package snd.komelia

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import io.github.snd_r.komelia.ui.dialogs.komf.reset.ResetDialog
import io.github.snd_r.komelia.ui.dialogs.komf.reset.resetLibraryText
import io.github.snd_r.komelia.ui.dialogs.komf.reset.resetSeriesText
import snd.komf.api.KomfServerLibraryId
import snd.komf.api.KomfServerSeriesId

@Composable
fun ResetSeriesMetadataDialog(
    seriesId: KomfServerSeriesId?,
    libraryId: KomfServerLibraryId?,
    onDismissRequest: () -> Unit,
) {
    if (seriesId == null || seriesId.value.isBlank() || libraryId == null || libraryId.value.isBlank()) {
        ErrorDialog(seriesId, libraryId, onDismissRequest)
    } else {
        val viewModelFactory = LocalKomfViewModelFactory.current
        val vm = remember { viewModelFactory.getKomfResetMetadataDialogViewModel(onDismissRequest) }
        ResetDialog(
            dialogText = resetSeriesText,
            removeComicInfo = vm.removeComicInfo,
            onRemoveComicInfoChange = vm::removeComicInfo::set,
            onConfirm = { vm.onSeriesReset(seriesId, libraryId) },
            onDismissRequest = onDismissRequest
        )
    }
}

@Composable
fun ResetLibraryMetadataDialog(
    libraryId: KomfServerLibraryId?,
    onDismissRequest: () -> Unit,
) {
    if (libraryId == null || libraryId.value.isBlank()) {
        ErrorDialog(libraryId, onDismissRequest)
    } else {
        val viewModelFactory = LocalKomfViewModelFactory.current
        val vm = remember { viewModelFactory.getKomfResetMetadataDialogViewModel(onDismissRequest) }
        ResetDialog(
            dialogText = resetLibraryText,
            removeComicInfo = vm.removeComicInfo,
            onRemoveComicInfoChange = vm::removeComicInfo::set,
            onConfirm = { vm.onLibraryReset(libraryId) },
            onDismissRequest = onDismissRequest
        )
    }
}
