package snd.komelia.ui.dialogs.series.edit

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import io.github.snd_r.komelia.ui.komelia_ui.generated.resources.Res
import io.github.snd_r.komelia.ui.komelia_ui.generated.resources.dialog_edit
import io.github.snd_r.komelia.ui.komelia_ui.generated.resources.dialog_save
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.stringResource
import snd.komelia.ui.LocalViewModelFactory
import snd.komelia.ui.dialogs.oneshot.OneshotEditDialog
import snd.komelia.ui.dialogs.tabs.TabDialog
import snd.komga.client.series.KomgaSeries

@Composable
fun SeriesEditDialog(
    series: KomgaSeries,
    onDismissRequest: () -> Unit
) {
    val viewModelFactory = LocalViewModelFactory.current
    val vm = remember { viewModelFactory.getSeriesEditDialogViewModel(series, onDismissRequest) }
    LaunchedEffect(series) { vm.initialize() }

    val coroutineScope = rememberCoroutineScope()
    if (series.oneshot) {
        OneshotEditDialog(series.id, series, null, onDismissRequest)
    } else {
        TabDialog(
            title = stringResource(Res.string.dialog_edit, series.metadata.title),
            currentTab = vm.currentTab,
            tabs = vm.tabs,
            confirmationText = stringResource(Res.string.dialog_save),
            onConfirm = { coroutineScope.launch { vm.saveChanges() } },
            onTabChange = { vm.currentTab = it },
            onDismissRequest = { onDismissRequest() }
        )
    }
}
