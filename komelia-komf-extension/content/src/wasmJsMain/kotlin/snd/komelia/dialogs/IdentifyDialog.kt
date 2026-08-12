package snd.komelia.dialogs

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import snd.komelia.LocalKomfViewModelFactory
import snd.komelia.ui.LoadState
import snd.komelia.ui.dialogs.AppDialog
import snd.komelia.ui.dialogs.DialogConfirmCancelButtons
import snd.komelia.ui.dialogs.DialogSimpleHeader
import snd.komelia.ui.dialogs.komf.identify.IdentificationProgressButtons
import snd.komelia.ui.dialogs.komf.identify.IdentificationProgressContent
import snd.komelia.ui.dialogs.komf.identify.IdentifyConfigButtons
import snd.komelia.ui.dialogs.komf.identify.IdentifyConfigContent
import snd.komelia.ui.dialogs.komf.identify.IdentifyResultsContent
import snd.komelia.ui.dialogs.komf.identify.IdentifySearchResultsButtons
import snd.komelia.ui.dialogs.komf.identify.KomfIdentifyDialogViewModel.IdentifyTab.IDENTIFICATION_PROGRESS
import snd.komelia.ui.dialogs.komf.identify.KomfIdentifyDialogViewModel.IdentifyTab.IDENTIFY_SETTINGS
import snd.komelia.ui.dialogs.komf.identify.KomfIdentifyDialogViewModel.IdentifyTab.SEARCH_RESULTS
import snd.komf.api.KomfServerLibraryId
import snd.komf.api.KomfServerSeriesId
import snd.komf.api.MediaServer

@Composable
fun IdentifyDialog(
    mediaServer: MediaServer,
    seriesId: KomfServerSeriesId,
    libraryId: KomfServerLibraryId,
    seriesName: String?,
    onDismissRequest: () -> Unit,
) {
    val viewModelFactory = LocalKomfViewModelFactory.current
    val vm = remember {
        viewModelFactory.getKomfIdentifyDialogViewModel(
            seriesId = seriesId,
            libraryId = libraryId,
            seriesName = seriesName ?: "",
            mediaServer = mediaServer,
            onDismissRequest = onDismissRequest,
        )
    }
    val state = vm.state.collectAsState().value
    val isLoading = derivedStateOf { state == LoadState.Loading }
    DisposableEffect(Unit) { onDispose { vm.onDispose() } }

    AppDialog(
        modifier = Modifier.widthIn(max = 840.dp),
        header = { DialogSimpleHeader("Identify") },
        content = {
            Box(
                modifier = Modifier.fillMaxSize().padding(vertical = 10.dp),
                contentAlignment = Alignment.Center
            ) {
                when (vm.currentTab) {
                    IDENTIFY_SETTINGS -> IdentifyConfigContent(vm.configState)
                    SEARCH_RESULTS -> IdentifyResultsContent(vm.searchResultsState)
                    IDENTIFICATION_PROGRESS -> IdentificationProgressContent(vm.identificationState)
                }
            }
        },
        controlButtons = {
            when (vm.currentTab) {
                IDENTIFY_SETTINGS -> IdentifyConfigButtons(vm.configState)
                SEARCH_RESULTS -> IdentifySearchResultsButtons(vm.searchResultsState)
                IDENTIFICATION_PROGRESS -> IdentificationProgressButtons(vm.identificationState, isLoading.value)
            }
        },
        onDismissRequest = { if (!isLoading.value) onDismissRequest() },
        contentPadding = PaddingValues(20.dp)
    )
}

@Composable
fun LibraryAutoIdentifyDialog(
    mediaServer: MediaServer,
    libraryId: KomfServerLibraryId,
    onDismissRequest: () -> Unit,
) {
    val viewModelFactory = LocalKomfViewModelFactory.current
    val vm = remember {
        viewModelFactory.getKomfLibraryIdentifyViewModel(libraryId = libraryId, mediaServer = mediaServer)
    }

    AppDialog(
        modifier = Modifier.widthIn(max = 840.dp),
        header = { DialogSimpleHeader("Auto-Identify") },
        content = {
            Box(
                modifier = Modifier.fillMaxSize().padding(vertical = 10.dp),
                contentAlignment = Alignment.Center,
            ) {
                Text("Launch auto identification job for entire library? This action might take a long time for big libraries\nContinue?")
            }
        },
        controlButtons = {
            DialogConfirmCancelButtons(
                onConfirm = {
                    vm.autoIdentify()
                    onDismissRequest()
                },
                onCancel = {
                    onDismissRequest()
                },
            )
        },
        onDismissRequest = { onDismissRequest() },
        contentPadding = PaddingValues(20.dp)
    )
}
