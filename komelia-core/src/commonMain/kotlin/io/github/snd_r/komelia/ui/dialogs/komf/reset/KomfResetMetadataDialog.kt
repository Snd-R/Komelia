package io.github.snd_r.komelia.ui.dialogs.komf.reset

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import io.github.snd_r.komelia.ui.LocalViewModelFactory
import io.github.snd_r.komelia.ui.common.SwitchWithLabel
import io.github.snd_r.komelia.ui.dialogs.AppDialog
import io.github.snd_r.komelia.ui.dialogs.DialogConfirmCancelButtons
import io.github.snd_r.komelia.ui.dialogs.DialogSimpleHeader
import kotlinx.coroutines.launch
import snd.komf.api.KomfServerLibraryId
import snd.komf.api.KomfServerSeriesId
import snd.komga.client.library.KomgaLibrary
import snd.komga.client.series.KomgaSeries

val resetSeriesText = """
    All series metadata will be reset including field locks and thumbnails uploaded by Komf.
    No files will be modified. Continue?
""".trimIndent()
val resetLibraryText = """
    All metadata of all series inside this library will be reset including field locks and thumbnails uploaded by Komf. No files will be modified. Continue?
""".trimIndent()

@Composable
fun KomfResetSeriesMetadataDialog(
    series: KomgaSeries,
    onDismissRequest: () -> Unit,
) {
    KomfResetSeriesMetadataDialog(
        seriesId = KomfServerSeriesId(value = series.id.value),
        libraryId = KomfServerLibraryId(series.libraryId.value),
        onDismissRequest = onDismissRequest
    )
}

@Composable
fun KomfResetSeriesMetadataDialog(
    seriesId: KomfServerSeriesId,
    libraryId: KomfServerLibraryId,
    onDismissRequest: () -> Unit,
) {
    val viewModelFactory = LocalViewModelFactory.current
    val vm = remember { viewModelFactory.getKomfResetMetadataDialogViewModel(onDismissRequest) }
    ResetDialog(
        dialogText = resetSeriesText,
        removeComicInfo = vm.removeComicInfo,
        onRemoveComicInfoChange = vm::removeComicInfo::set,
        onConfirm = { vm.onSeriesReset(seriesId, libraryId) },
        onDismissRequest = onDismissRequest
    )
}

@Composable
fun KomfResetLibraryMetadataDialog(
    library: KomgaLibrary,
    onDismissRequest: () -> Unit,
) {
    KomfResetLibraryMetadataDialog(KomfServerLibraryId(library.id.value), onDismissRequest)
}

@Composable
fun KomfResetLibraryMetadataDialog(
    libraryId: KomfServerLibraryId,
    onDismissRequest: () -> Unit,
) {
    val viewModelFactory = LocalViewModelFactory.current
    val vm = remember { viewModelFactory.getKomfResetMetadataDialogViewModel(onDismissRequest) }
    ResetDialog(
        dialogText = resetLibraryText,
        removeComicInfo = vm.removeComicInfo,
        onRemoveComicInfoChange = vm::removeComicInfo::set,
        onConfirm = { vm.onLibraryReset(libraryId) },
        onDismissRequest = onDismissRequest
    )
}

@Composable
fun ResetDialog(
    dialogText: String,
    removeComicInfo: Boolean,
    onRemoveComicInfoChange: (Boolean) -> Unit,
    onConfirm: suspend () -> Unit,
    onDismissRequest: () -> Unit,
) {
    val coroutineScope = rememberCoroutineScope()
    var isLoading by remember { mutableStateOf(false) }
    AppDialog(
        contentPadding = PaddingValues(20.dp),
        modifier = Modifier.widthIn(max = 650.dp),
        header = { DialogSimpleHeader("Reset Library Metadata") },
        content = { DialogContent(dialogText, removeComicInfo, onRemoveComicInfoChange) },
        controlButtons = {
            DialogConfirmCancelButtons(
                onConfirm = {
                    coroutineScope.launch {
                        isLoading = true
                        onConfirm()
                        onDismissRequest()
                    }
                },
                onCancel = onDismissRequest,
                isLoading = isLoading
            )
        },
        onDismissRequest = onDismissRequest
    )
}

@Composable
private fun DialogContent(
    dialogText: String,
    removeComicInfo: Boolean,
    onRemoveComicInfoChange: (Boolean) -> Unit,
) {
    Column(
        modifier = Modifier.heightIn(min = 200.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Text(text = dialogText)
        SwitchWithLabel(
            checked = removeComicInfo,
            onCheckedChange = onRemoveComicInfoChange,
            label = { Text("Remove ComicInfo.xml?") },
            supportingText = { Text("Requires write access to files") }
        )
    }
}