package snd.komelia.ui.dialogs.komf.mangabaka

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import snd.komelia.komga.api.model.KomeliaSeries
import snd.komelia.ui.LocalViewModelFactory
import snd.komelia.ui.dialogs.ConfirmationDialog


@Composable
fun KomfMangaBakaUnlinkDialog(
    series: KomeliaSeries,
    onDismissRequest: () -> Unit,
) {
    val viewModelFactory = LocalViewModelFactory.current
    val vm = remember { viewModelFactory.getKomfMangaBakaUnlinkDialogViewModel(series, onDismissRequest) }
    DisposableEffect(Unit) { onDispose { vm.onDispose() } }
    ConfirmationDialog(
        body = "Unlink this series?",
        onDialogConfirm = vm::onUnlink,
        dismissOnConfirm = false,
        onDialogDismiss = onDismissRequest
    )
}
