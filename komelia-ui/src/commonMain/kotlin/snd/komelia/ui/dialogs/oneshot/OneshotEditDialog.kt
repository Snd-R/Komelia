package snd.komelia.ui.dialogs.oneshot

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.launch
import snd.komelia.komga.api.model.KomeliaBook
import snd.komelia.ui.LoadState.Error
import snd.komelia.ui.LoadState.Loading
import snd.komelia.ui.LoadState.Success
import snd.komelia.ui.LoadState.Uninitialized
import snd.komelia.ui.LocalViewModelFactory
import snd.komelia.ui.dialogs.DialogLoadIndicator
import snd.komelia.ui.dialogs.tabs.TabDialog
import snd.komga.client.series.KomgaSeries
import snd.komga.client.series.KomgaSeriesId

@Composable
fun OneshotEditDialog(
    seriesId: KomgaSeriesId,
    series: KomgaSeries?,
    book: KomeliaBook?,
    onDismissRequest: () -> Unit,
) {
    val viewModelFactory = LocalViewModelFactory.current
    val coroutineScope = rememberCoroutineScope()
    val vm = remember {
        viewModelFactory.getOneshotEditDialogViewModel(seriesId, series, book, onDismissRequest)
    }
    LaunchedEffect(book) { vm.initialize() }
    when (val loadState = vm.loadState.collectAsState().value) {
        Uninitialized, Loading -> DialogLoadIndicator(onDismissRequest)
        is Success -> TabDialog(
            title = "Edit ${loadState.value.seriesMetadataState.series.metadata.title}",
            currentTab = loadState.value.currentTab,
            tabs = loadState.value.tabs,
            confirmationText = "Save",
            onConfirm = { coroutineScope.launch { vm.saveChanges() } },
            onTabChange = { loadState.value.currentTab = it },
            onDismissRequest = onDismissRequest
        )

        is Error -> onDismissRequest()
    }

}
