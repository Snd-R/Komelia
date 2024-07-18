package io.github.snd_r.komelia.ui.series.list

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import io.github.snd_r.komelia.platform.WindowWidth.COMPACT
import io.github.snd_r.komelia.platform.WindowWidth.EXPANDED
import io.github.snd_r.komelia.platform.WindowWidth.FULL
import io.github.snd_r.komelia.platform.WindowWidth.MEDIUM
import io.github.snd_r.komelia.platform.cursorForHand
import io.github.snd_r.komelia.ui.LocalWindowWidth
import io.github.snd_r.komelia.ui.common.PageSizeSelectionDropdown
import io.github.snd_r.komelia.ui.common.itemlist.SeriesLazyCardGrid
import io.github.snd_r.komelia.ui.common.menus.SeriesMenuActions
import io.github.snd_r.komelia.ui.common.menus.bulk.BottomPopupBulkActionsPanel
import io.github.snd_r.komelia.ui.common.menus.bulk.BulkActionsContainer
import io.github.snd_r.komelia.ui.common.menus.bulk.SeriesBulkActionsContent
import io.github.snd_r.komelia.ui.series.SeriesFilterState
import io.github.snd_r.komelia.ui.series.view.SeriesFilterContent
import io.github.snd_r.komga.series.KomgaSeries

@Composable
fun SeriesListContent(
    series: List<KomgaSeries>,
    seriesTotalCount: Int,
    seriesActions: SeriesMenuActions,
    onSeriesClick: (KomgaSeries) -> Unit,

    editMode: Boolean,
    onEditModeChange: (Boolean) -> Unit,
    selectedSeries: List<KomgaSeries>,
    onSeriesSelect: (KomgaSeries) -> Unit,

    isLoading: Boolean,
    filterState: SeriesFilterState?,

    totalPages: Int,
    currentPage: Int,
    pageSize: Int,
    onPageChange: (Int) -> Unit,
    onPageSizeChange: (Int) -> Unit,

    minSize: Dp,
) {
    Column {
        if (editMode) {
            BulkActionsToolbar(
                onCancel = { onEditModeChange(false) },
                series = series,
                selectedSeries = selectedSeries,
                onSeriesSelect = onSeriesSelect
            )
        }

        SeriesLazyCardGrid(
            series = series,
            onSeriesClick = if (editMode) onSeriesSelect else onSeriesClick,
            seriesMenuActions = if (editMode) null else seriesActions,

            selectedSeries = selectedSeries,
            onSeriesSelect = onSeriesSelect,

            totalPages = totalPages,
            currentPage = currentPage,
            onPageChange = onPageChange,

            beforeContent = {
                AnimatedVisibility(!editMode) {
                    ToolBar(
                        seriesTotalCount = seriesTotalCount,
                        pageSize = pageSize,
                        onPageSizeChange = onPageSizeChange,
                        isLoading = isLoading,
                        filterState = filterState
                    )
                }

            },
            minSize = minSize,
        )
        val width = LocalWindowWidth.current
        if ((width == COMPACT || width == MEDIUM) && selectedSeries.isNotEmpty()) {
            BottomPopupBulkActionsPanel {
                SeriesBulkActionsContent(selectedSeries, false)
            }
        }
    }
}

@Composable
private fun BulkActionsToolbar(
    onCancel: () -> Unit,
    series: List<KomgaSeries>,
    selectedSeries: List<KomgaSeries>,
    onSeriesSelect: (KomgaSeries) -> Unit,
) {
    BulkActionsContainer(
        onCancel = onCancel,
        selectedCount = selectedSeries.size,
        allSelected = series.size == selectedSeries.size,
        onSelectAll = {
            if (series.size == selectedSeries.size) series.forEach { onSeriesSelect(it) }
            else series.filter { it !in selectedSeries }.forEach { onSeriesSelect(it) }
        }
    ) {
        when (LocalWindowWidth.current) {
            FULL -> {
                Text("Selection mode: Click on items to select or deselect them")
                if (selectedSeries.isNotEmpty()) {
                    Spacer(Modifier.weight(1f))
                    SeriesBulkActionsContent(selectedSeries, true)
                }
            }

            EXPANDED -> {
                if (selectedSeries.isEmpty()) {
                    Text("Selection mode: Click on items to select or deselect them")
                } else {
                    Spacer(Modifier.weight(1f))
                    SeriesBulkActionsContent(selectedSeries, true)
                }
            }

            COMPACT, MEDIUM -> {}
        }
    }
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
                trackColor = Color.Transparent,
                modifier = Modifier.fillMaxWidth(),
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

            if (seriesTotalCount != 0) {
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

                PageSizeSelectionDropdown(pageSize, onPageSizeChange)
            }
        }
    }
}
