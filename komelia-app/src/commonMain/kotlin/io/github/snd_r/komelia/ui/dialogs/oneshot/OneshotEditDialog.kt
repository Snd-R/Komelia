package io.github.snd_r.komelia.ui.dialogs.oneshot

import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import io.github.snd_r.komelia.ui.LoadState.Error
import io.github.snd_r.komelia.ui.LoadState.Loading
import io.github.snd_r.komelia.ui.LoadState.Success
import io.github.snd_r.komelia.ui.LoadState.Uninitialized
import io.github.snd_r.komelia.ui.LocalViewModelFactory
import io.github.snd_r.komelia.ui.common.LoadingMaxSizeIndicator
import io.github.snd_r.komelia.ui.dialogs.AppDialog
import io.github.snd_r.komelia.ui.dialogs.tabs.TabDialog
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import snd.komga.client.book.KomgaBook
import snd.komga.client.series.KomgaSeries
import snd.komga.client.series.KomgaSeriesId

@Composable
fun OneshotEditDialog(
    seriesId: KomgaSeriesId,
    series: KomgaSeries?,
    book: KomgaBook?,
    onDismissRequest: () -> Unit,
) {
    val viewModelFactory = LocalViewModelFactory.current
    val coroutineScope = rememberCoroutineScope()
    val vm = remember {
        viewModelFactory.getOneshotEditDialogViewModel(seriesId, series, book, onDismissRequest)
    }
    LaunchedEffect(book) { vm.initialize() }
    when (val loadState = vm.loadState.collectAsState().value) {
        Uninitialized, Loading -> LoadingContent(onDismissRequest)
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

@Composable
private fun LoadingContent(onDismissRequest: () -> Unit) {
    var showLoadIndicator by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        delay(200)
        showLoadIndicator = true
    }
    if (showLoadIndicator) {
        AppDialog(
            modifier = Modifier.size(500.dp),
            content = { LoadingMaxSizeIndicator() },
            onDismissRequest = onDismissRequest,
        )
    }
}
