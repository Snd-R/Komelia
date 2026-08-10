package snd.komelia.ui.dialogs.series.editbulk

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import io.github.snd_r.komelia.ui.komelia_ui.generated.resources.Res
import io.github.snd_r.komelia.ui.komelia_ui.generated.resources.dialog_save
import io.github.snd_r.komelia.ui.komelia_ui.generated.resources.series_edit_bulk_dialog_title
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.pluralStringResource
import org.jetbrains.compose.resources.stringResource
import snd.komelia.ui.LocalViewModelFactory
import snd.komelia.ui.dialogs.tabs.TabDialog
import snd.komga.client.series.KomgaSeries

@Composable
fun SeriesBulkEditDialog(
    series: List<KomgaSeries>,
    onDismissRequest: () -> Unit
) {
    val viewModelFactory = LocalViewModelFactory.current
    val vm = remember { viewModelFactory.getSeriesBulkEditDialogViewModel(series, onDismissRequest) }
    LaunchedEffect(series) { vm.initialize() }

    val coroutineScope = rememberCoroutineScope()
    TabDialog(
        title = pluralStringResource(Res.plurals.series_edit_bulk_dialog_title, series.size, series.size),
        currentTab = vm.currentTab,
        tabs = vm.tabs(),
        confirmationText = stringResource(Res.string.dialog_save),
        onConfirm = { coroutineScope.launch { vm.saveChanges() } },
        onTabChange = { vm.currentTab = it },
        onDismissRequest = { onDismissRequest() }
    )
}