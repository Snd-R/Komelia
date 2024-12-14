package io.github.snd_r.komelia.ui.dialogs.series.edit

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import io.github.snd_r.komelia.ui.LocalViewModelFactory
import io.github.snd_r.komelia.ui.dialogs.oneshot.OneshotEditDialog
import io.github.snd_r.komelia.ui.dialogs.tabs.TabDialog
import kotlinx.coroutines.launch
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
            title = "Edit ${series.metadata.title}",
            currentTab = vm.currentTab,
            tabs = vm.tabs,
            confirmationText = "Save",
            onConfirm = { coroutineScope.launch { vm.saveChanges() } },
            onTabChange = { vm.currentTab = it },
            onDismissRequest = { onDismissRequest() }
        )
    }
}
