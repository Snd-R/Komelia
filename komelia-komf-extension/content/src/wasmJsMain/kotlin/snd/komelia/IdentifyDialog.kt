package snd.komelia

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import io.github.snd_r.komelia.ui.LoadState
import io.github.snd_r.komelia.ui.dialogs.AppDialog
import io.github.snd_r.komelia.ui.dialogs.DialogConfirmCancelButtons
import io.github.snd_r.komelia.ui.dialogs.DialogSimpleHeader
import io.github.snd_r.komelia.ui.dialogs.komf.identify.*
import io.github.snd_r.komelia.ui.dialogs.komf.identify.KomfIdentifyDialogViewModel.IdentifyTab.*
import snd.komf.api.KomfServerLibraryId
import snd.komf.api.KomfServerSeriesId

@Composable
fun IdentifyDialog(
    seriesId: KomfServerSeriesId?,
    libraryId: KomfServerLibraryId?,
    seriesName: String?,
    onDismissRequest: () -> Unit,
) {
    if (seriesId == null || seriesId.value.isBlank() || libraryId == null || libraryId.value.isBlank()) {
        ErrorDialog(seriesId, libraryId, onDismissRequest)
        return
    }

    val viewModelFactory = LocalKomfViewModelFactory.current
    val vm = remember {
        viewModelFactory.getKomfIdentifyDialogViewModel(
            seriesId = seriesId,
            libraryId = libraryId,
            seriesName = seriesName ?: "",
            onDismissRequest = onDismissRequest
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
                    IDENTIFY_SETTINGS -> IdenitfyConfigContent(vm.configState)
                    SEARCH_RESULTS -> IdentiufyResultsContent(vm.searchResultsState)
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
    libraryId: KomfServerLibraryId?,
    onDismissRequest: () -> Unit,
) {
    if (libraryId == null || libraryId.value.isBlank()) {
        ErrorDialog(libraryId, onDismissRequest)
        return
    }

    val viewModelFactory = LocalKomfViewModelFactory.current
    val vm = remember {
        viewModelFactory.getKomfLibraryIdentifyViewModel(libraryId = libraryId)
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
