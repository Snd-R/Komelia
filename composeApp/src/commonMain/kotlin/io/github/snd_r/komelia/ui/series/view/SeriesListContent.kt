package io.github.snd_r.komelia.ui.series.view

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import io.github.snd_r.komelia.platform.cursorForHand
import io.github.snd_r.komelia.ui.common.DropdownChoiceMenu
import io.github.snd_r.komelia.ui.common.LabeledEntry.Companion.intEntry
import io.github.snd_r.komelia.ui.common.itemlist.SeriesLazyCardGrid
import io.github.snd_r.komelia.ui.common.menus.SeriesMenuActions
import io.github.snd_r.komelia.ui.series.SeriesFilterState
import io.github.snd_r.komga.series.KomgaSeries
import io.github.snd_r.komga.series.KomgaSeriesId

@Composable
fun SeriesListContent(
    series: List<KomgaSeries>,
    seriesTotalCount: Int,
    seriesActions: SeriesMenuActions,
    onSeriesClick: (KomgaSeriesId) -> Unit,
    isLoading: Boolean,

    filterState: SeriesFilterState?,

    totalPages: Int,
    currentPage: Int,
    pageSize: Int,
    onPageChange: (Int) -> Unit,
    onPageSizeChange: (Int) -> Unit,

    minSize: Dp,
) {
    SeriesLazyCardGrid(
        series = series,
        seriesMenuActions = seriesActions,
        minSize = minSize,
        onSeriesClick = onSeriesClick,
        totalPages = totalPages,
        currentPage = currentPage,
        onPageChange = onPageChange,
        beforeContent = {
            ToolBar(
                seriesTotalCount = seriesTotalCount,
                pageSize = pageSize,
                onPageSizeChange = onPageSizeChange,
                isLoading = isLoading,
                filterState = filterState
            )
        }
    )
}

@Composable
private fun ToolBar(
    seriesTotalCount: Int,
    pageSize: Int,
    onPageSizeChange: (Int) -> Unit,
    isLoading: Boolean,
    filterState: SeriesFilterState?,
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        var showFilters by remember { mutableStateOf(false) }
        if (isLoading) {
            LinearProgressIndicator(
                color = MaterialTheme.colorScheme.tertiaryContainer,
                modifier = Modifier.fillMaxWidth()
            )
        } else {
            Spacer(Modifier.size(4.dp))
        }

        if (filterState != null) {
            AnimatedVisibility(visible = showFilters) {
                SeriesFilterContent(
                    filterState = filterState,
                    onDismiss = { showFilters = false }
                )
            }
        }

        Row(
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth().padding(horizontal = 10.dp)
        ) {

            SuggestionChip(
                onClick = {},
                label = { Text("$seriesTotalCount series") },
            )

            Spacer(Modifier.weight(1f))

            if (filterState != null) {
                val color =
                    if (filterState.isChanged) MaterialTheme.colorScheme.tertiary
                    else MaterialTheme.colorScheme.primary

                IconButton(onClick = { showFilters = !showFilters }, modifier = Modifier.cursorForHand()) {
                    Icon(Icons.Default.FilterList, null, tint = color)
                }
            }

            DropdownChoiceMenu(
                selectedOption = intEntry(pageSize),
                options = listOf(
                    intEntry(20),
                    intEntry(50),
                    intEntry(100),
                    intEntry(200),
                    intEntry(500)
                ),
                onOptionChange = { onPageSizeChange(it.value) },
                contentPadding = PaddingValues(5.dp),
                textFieldModifier = Modifier
                    .widthIn(min = 70.dp)
                    .clip(RoundedCornerShape(5.dp))
                    .padding(end = 10.dp)
            )

        }
    }
}
