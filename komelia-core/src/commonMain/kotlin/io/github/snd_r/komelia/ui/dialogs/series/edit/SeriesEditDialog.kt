package io.github.snd_r.komelia.ui.dialogs.series.edit

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import io.github.snd_r.komelia.ui.LoadState
import io.github.snd_r.komelia.ui.LocalViewModelFactory
import io.github.snd_r.komelia.ui.dialogs.DialogLoadIndicator
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
        when (val loadState = vm.loadState.collectAsState().value) {
            LoadState.Uninitialized, LoadState.Loading -> DialogLoadIndicator(onDismissRequest)
            is LoadState.Success -> TabDialog(
                title = "Edit ${series.metadata.title}",
                currentTab = loadState.value.currentTab,
                tabs = loadState.value.tabs,
                confirmationText = "Save",
                onConfirm = { coroutineScope.launch { vm.saveChanges() } },
                onTabChange = { loadState.value.currentTab = it },
                onDismissRequest = { onDismissRequest() }
            )

            is LoadState.Error -> onDismissRequest()
        }
    }
}
