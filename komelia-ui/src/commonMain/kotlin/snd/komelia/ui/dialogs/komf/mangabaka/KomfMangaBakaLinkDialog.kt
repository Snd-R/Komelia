package snd.komelia.ui.dialogs.komf.mangabaka

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import io.github.snd_r.komelia.ui.komelia_ui.generated.resources.Res
import io.github.snd_r.komelia.ui.komelia_ui.generated.resources.komf_identify
import io.github.snd_r.komelia.ui.komelia_ui.generated.resources.komf_identify_cancel
import io.github.snd_r.komelia.ui.komelia_ui.generated.resources.komf_identify_confirm
import io.github.snd_r.komelia.ui.komelia_ui.generated.resources.komf_identify_no_results
import io.github.snd_r.komelia.ui.komelia_ui.generated.resources.komf_identify_search
import io.github.snd_r.komelia.ui.komelia_ui.generated.resources.komf_identify_title
import org.jetbrains.compose.resources.stringResource
import snd.komelia.komga.api.model.KomeliaSeries
import snd.komelia.ui.LoadState
import snd.komelia.ui.LocalViewModelFactory
import snd.komelia.ui.common.cards.KomfMangaBakaResultCard
import snd.komelia.ui.dialogs.AppDialog
import snd.komelia.ui.dialogs.DialogSimpleHeader
import snd.komelia.ui.dialogs.komf.mangabaka.KomfMangaBakaLinkViewModel.BakaLinkTab.SEARCH
import snd.komelia.ui.dialogs.komf.mangabaka.KomfMangaBakaLinkViewModel.BakaLinkTab.SEARCH_RESULTS
import snd.komelia.ui.platform.cursorForHand
import snd.komf.api.mangabaka.KomfMangaBakaSeries
import snd.komf.api.mangabaka.MangaBakaSeriesId


@Composable
fun KomfMangaBakaLinkDialog(
    series: KomeliaSeries,
    onDismissRequest: () -> Unit,
) {
    val viewModelFactory = LocalViewModelFactory.current
    val vm = remember { viewModelFactory.getKomfMangaBakaLinkDialogViewModel(series, onDismissRequest) }
    DisposableEffect(Unit) { onDispose { vm.onDispose() } }
    val series = vm.series
    val state = vm.state.collectAsState().value
    val isLoading by remember { derivedStateOf { state == LoadState.Loading } }
    val currentTab = vm.currentTab.collectAsState().value
    val selectedResult = vm.selectedResult.collectAsState().value
    AppDialog(
        modifier = Modifier.widthIn(max = 840.dp),
        header = { DialogSimpleHeader(stringResource(Res.string.komf_identify)) },
        content = {
            Box(
                modifier = Modifier.fillMaxSize().padding(vertical = 10.dp),
                contentAlignment = Alignment.Center
            ) {
                when (currentTab) {
                    SEARCH -> SearchTabContent(
                        series = series,
                        onSearch = vm::onSearch,
                        onLinkById = vm::onLink,
                        isLoading = isLoading
                    )

                    SEARCH_RESULTS -> SearchResultsContent(
                        results = vm.searchResults.collectAsState().value,
                        selectedResult = selectedResult,
                        onSelect = vm::onResultSelect
                    )
                }
            }
        },
        controlButtons = {
            when (currentTab) {
                SEARCH -> SearchTabControlButtons(onDismissRequest)
                SEARCH_RESULTS -> SearchResultsControlButtons(
                    selectedResult = selectedResult,
                    onLink = vm::onLink,
                    isLoading = isLoading,
                    onDismissRequest = onDismissRequest
                )
            }
        },
        onDismissRequest = { if (!isLoading) onDismissRequest() },
        contentPadding = PaddingValues(20.dp)
    )
}

@Composable
private fun SearchTabContent(
    series: KomeliaSeries,
    onSearch: (String) -> Unit,
    onLinkById: (MangaBakaSeriesId) -> Unit,
    isLoading: Boolean
) {
    Column(
        modifier = Modifier.heightIn(min = 200.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            var titleText by remember { mutableStateOf(series.metadata.title) }
            TextField(
                value = titleText,
                onValueChange = { titleText = it },
                label = { Text(stringResource(Res.string.komf_identify_title)) },
                modifier = Modifier.weight(1f)
            )

            FilledTonalButton(
                modifier = Modifier.cursorForHand(),
                onClick = { onSearch(titleText) },
                enabled = !isLoading,
            ) {
                if (isLoading) CircularProgressIndicator(Modifier.size(25.dp))
                else Text(stringResource(Res.string.komf_identify_search))
            }
        }

        Row(
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            var seriesIdText by remember { mutableStateOf("") }
            TextField(
                value = seriesIdText,
                onValueChange = { newText ->
                    newText.toLongOrNull()?.let { seriesIdText = newText }
                },
                label = { Text("MangaBaka series ID") },
                modifier = Modifier.weight(1f)
            )

            FilledTonalButton(
                modifier = Modifier.cursorForHand(),
                onClick = {
                    seriesIdText.toLongOrNull()?.let {
                        onLinkById(MangaBakaSeriesId(it))
                    }
                },
                enabled = !isLoading,
            ) {
                if (isLoading) CircularProgressIndicator(Modifier.size(25.dp))
                else Text("Link by ID")
            }
        }
    }
}

@Composable
private fun SearchTabControlButtons(onDismissRequest: () -> Unit) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        ElevatedButton(
            onClick = onDismissRequest,
            modifier = Modifier.cursorForHand()
        ) {
            Text(stringResource(Res.string.komf_identify_cancel))
        }
    }
}

@Composable
fun SearchResultsContent(
    results: List<KomfMangaBakaSeries>,
    selectedResult: KomfMangaBakaSeries?,
    onSelect: (KomfMangaBakaSeries) -> Unit,
) {
    if (results.isEmpty()) {
        Text(stringResource(Res.string.komf_identify_no_results))
        return
    }
    FlowRow(
        verticalArrangement = Arrangement.spacedBy(20.dp),
        horizontalArrangement = Arrangement.spacedBy(20.dp),
    ) {
        results.forEach { result ->
            KomfMangaBakaResultCard(
                modifier = Modifier.widthIn(max = 180.dp),
                result = result,
                isSelected = result.id == selectedResult?.id,
                onClick = { onSelect(result) },
            )
        }
    }
}

@Composable
fun SearchResultsControlButtons(
    selectedResult: KomfMangaBakaSeries?,
    onLink: (KomfMangaBakaSeries) -> Unit,
    isLoading: Boolean,
    onDismissRequest: () -> Unit,
) {
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
            enabled = !isLoading,
            onClick = { selectedResult?.let { onLink(it) } },
            modifier = Modifier.cursorForHand()
        ) {
            if (isLoading) CircularProgressIndicator(Modifier.size(25.dp))
            else Text(stringResource(Res.string.komf_identify_confirm))
        }
    }
}
