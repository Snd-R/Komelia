package snd.komelia.ui.dialogs.komf.identify

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Error
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import io.github.snd_r.komelia.ui.komelia_ui.generated.resources.Res
import io.github.snd_r.komelia.ui.komelia_ui.generated.resources.komf_identify
import io.github.snd_r.komelia.ui.komelia_ui.generated.resources.komf_identify_auto_identify
import io.github.snd_r.komelia.ui.komelia_ui.generated.resources.komf_identify_cancel
import io.github.snd_r.komelia.ui.komelia_ui.generated.resources.komf_identify_completed
import io.github.snd_r.komelia.ui.komelia_ui.generated.resources.komf_identify_confirm
import io.github.snd_r.komelia.ui.komelia_ui.generated.resources.komf_identify_no_results
import io.github.snd_r.komelia.ui.komelia_ui.generated.resources.komf_identify_processing
import io.github.snd_r.komelia.ui.komelia_ui.generated.resources.komf_identify_run_in_background
import io.github.snd_r.komelia.ui.komelia_ui.generated.resources.komf_identify_search
import io.github.snd_r.komelia.ui.komelia_ui.generated.resources.komf_identify_title
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.stringResource
import snd.komelia.ui.LoadState
import snd.komelia.ui.LocalViewModelFactory
import snd.komelia.ui.common.cards.KomfResultCard
import snd.komelia.ui.dialogs.AppDialog
import snd.komelia.ui.dialogs.DialogSimpleHeader
import snd.komelia.ui.dialogs.komf.identify.KomfIdentifyDialogViewModel.ConfigState
import snd.komelia.ui.dialogs.komf.identify.KomfIdentifyDialogViewModel.IdentificationState
import snd.komelia.ui.dialogs.komf.identify.KomfIdentifyDialogViewModel.IdentificationState.ProgressStatus
import snd.komelia.ui.dialogs.komf.identify.KomfIdentifyDialogViewModel.IdentificationState.ProgressStatus.RUNNING
import snd.komelia.ui.dialogs.komf.identify.KomfIdentifyDialogViewModel.IdentificationState.ProviderProgressStatus
import snd.komelia.ui.dialogs.komf.identify.KomfIdentifyDialogViewModel.IdentifyTab.IDENTIFICATION_PROGRESS
import snd.komelia.ui.dialogs.komf.identify.KomfIdentifyDialogViewModel.IdentifyTab.IDENTIFY_SETTINGS
import snd.komelia.ui.dialogs.komf.identify.KomfIdentifyDialogViewModel.IdentifyTab.SEARCH_RESULTS
import snd.komelia.ui.dialogs.komf.identify.KomfIdentifyDialogViewModel.SearchResultsState
import snd.komelia.ui.platform.cursorForHand
import snd.komelia.ui.strings.AppStrings
import snd.komga.client.series.KomgaSeries

@Composable
fun KomfIdentifyDialog(
    series: KomgaSeries,
    onDismissRequest: () -> Unit,
) {
    val viewModelFactory = LocalViewModelFactory.current
    val vm = remember { viewModelFactory.getKomfIdentifyDialogViewModel(series, onDismissRequest) }
    val state = vm.state.collectAsState().value
    val isLoading = derivedStateOf { state == LoadState.Loading }
    DisposableEffect(Unit) { onDispose { vm.onDispose() } }

    AppDialog(
        modifier = Modifier.widthIn(max = 840.dp),
        header = { DialogSimpleHeader(stringResource(Res.string.komf_identify)) },
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
fun IdentifyConfigContent(state: ConfigState) {
    val coroutineScope = rememberCoroutineScope()
    val isLoading = state.isLoading.collectAsState(false)
    var searchInProgress by remember { mutableStateOf(false) }
    Column(
        modifier = Modifier.heightIn(min = 200.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            TextField(
                value = state.searchName,
                onValueChange = state::searchName::set,
                label = { Text(stringResource(Res.string.komf_identify_title)) },
                modifier = Modifier.weight(1f)
            )

            FilledTonalButton(
                modifier = Modifier.cursorForHand(),
                onClick = {
                    coroutineScope.launch {
                        searchInProgress = true
                        state.onSearch()
                        searchInProgress = false
                    }
                },
                enabled = !isLoading.value,
            ) {
                if (isLoading.value && searchInProgress) CircularProgressIndicator(Modifier.size(25.dp))
                else Text(stringResource(Res.string.komf_identify_search))
            }
        }
    }
}

@Composable
fun IdentifyResultsContent(
    state: SearchResultsState,
) {
    if (state.searchResults.isEmpty()) {
        Text(stringResource(Res.string.komf_identify_no_results))
        return
    }
    FlowRow(
        verticalArrangement = Arrangement.spacedBy(20.dp),
        horizontalArrangement = Arrangement.spacedBy(20.dp),
    ) {
        key(state.searchResults) {
            state.searchResults.forEach { result ->
                var resultImage by remember(result) { mutableStateOf<ByteArray?>(null) }
                KomfResultCard(
                    modifier = Modifier.widthIn(max = 180.dp),
                    result = result,
                    image = resultImage,
                    isSelected = result.resultId == state.selectedSearchResult?.resultId,
                    onClick = { state.selectedSearchResult = result },
                )

                LaunchedEffect(result) {
                    resultImage = state.getSeriesCover(result)
                }
            }
        }
    }
}

@Composable
fun IdentificationProgressContent(
    state: IdentificationState
) {
    Column(
        modifier = Modifier.fillMaxWidth().heightIn(min = 200.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        state.providersProgress.forEach { ProviderProgressCard(it) }

        val error = state.processingError
        if (error != null) {
            Text(error, color = MaterialTheme.colorScheme.error)
        } else if (state.postProcessing) {
            ProcessingProgressCard()
        }
    }
}

@Composable
private fun ProviderProgressCard(progress: ProviderProgressStatus) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(5.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .padding(5.dp)
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Text(AppStrings.forProvider(progress.provider))
            Spacer(Modifier.weight(1f))
            progress.message?.let { Text(it) }

            when (progress.status) {
                ProgressStatus.COMPLETED -> {
                    Text(stringResource(Res.string.komf_identify_completed))
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.secondary
                    )
                }

                ProgressStatus.ERROR -> {
                    Icon(
                        imageVector = Icons.Default.Error,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.error
                    )
                }

                else -> {}
            }
        }
        if (progress.status == RUNNING) {
            if (progress.totalProgress != null && progress.currentProgress != null) {
                LinearProgressIndicator(
                    modifier = Modifier.fillMaxWidth(),
                    progress = { (progress.currentProgress.toFloat() / progress.totalProgress.toFloat()) }
                )
            } else {
                LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
            }

        }
    }
}

@Composable
private fun ProcessingProgressCard() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(5.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .padding(5.dp)
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Text(stringResource(Res.string.komf_identify_processing))
            Spacer(Modifier.weight(1f))
        }
        LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
    }
}

@Composable
fun IdentifyConfigButtons(state: ConfigState) {
    val isLoading = state.isLoading.collectAsState(false)
    var autoIdentifyProgress by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()

    Row(
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        ElevatedButton(
            onClick = state.onDismiss,
            modifier = Modifier.cursorForHand()
        ) {
            Text(stringResource(Res.string.komf_identify_cancel))
        }

        FilledTonalButton(
            onClick = {
                coroutineScope.launch {
                    autoIdentifyProgress = true
                    state.onAutoIdentify()
                    autoIdentifyProgress = false
                }
            },
            enabled = !isLoading.value,
            modifier = Modifier.cursorForHand()
        ) {
            if (isLoading.value && autoIdentifyProgress) CircularProgressIndicator(Modifier.size(25.dp))
            else Text(stringResource(Res.string.komf_identify_auto_identify))
        }
    }
}

@Composable
fun IdentifySearchResultsButtons(state: SearchResultsState) {
    ControlButtons(
        confirmationText = stringResource(Res.string.komf_identify_confirm),
        onConfirm = { state.onResultConfirm() },
        onDismissRequest = state.onDismiss
    )
}

@Composable
fun IdentificationProgressButtons(
    state: IdentificationState,
    isLoading: Boolean
) {
    FilledTonalButton(
        onClick = state.onDismiss,
        modifier = Modifier.cursorForHand()
    ) {
        if (isLoading) Text(stringResource(Res.string.komf_identify_run_in_background))
        else Text(stringResource(Res.string.komf_identify_confirm))
    }
}

@Composable
private fun ControlButtons(
    confirmationText: String,
    onConfirm: suspend () -> Unit,
    onDismissRequest: () -> Unit,
) {
    val coroutineScope = rememberCoroutineScope()
    var isLoading by remember { mutableStateOf(false) }

    Row(
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        ElevatedButton(
            onClick = onDismissRequest,
            modifier = Modifier.cursorForHand()
        ) {
            Text(stringResource(Res.string.komf_identify_cancel))
        }

        FilledTonalButton(
            onClick = {
                coroutineScope.launch {
                    isLoading = true
                    onConfirm()
                    isLoading = false
                }
            },
            modifier = Modifier.cursorForHand()
        ) {
            if (isLoading) CircularProgressIndicator(Modifier.size(25.dp))
            else Text(confirmationText)
        }
    }

}
