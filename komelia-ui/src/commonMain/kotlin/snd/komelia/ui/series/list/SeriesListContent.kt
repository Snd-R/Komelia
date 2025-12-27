package snd.komelia.ui.series.list

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
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
import snd.komelia.ui.LocalWindowWidth
import snd.komelia.ui.common.components.PageSizeSelectionDropdown
import snd.komelia.ui.common.itemlist.SeriesLazyCardGrid
import snd.komelia.ui.common.menus.SeriesMenuActions
import snd.komelia.ui.common.menus.bulk.BottomPopupBulkActionsPanel
import snd.komelia.ui.common.menus.bulk.BulkActionsContainer
import snd.komelia.ui.common.menus.bulk.SeriesBulkActionsContent
import snd.komelia.ui.platform.WindowSizeClass.COMPACT
import snd.komelia.ui.platform.WindowSizeClass.EXPANDED
import snd.komelia.ui.platform.WindowSizeClass.FULL
import snd.komelia.ui.platform.WindowSizeClass.MEDIUM
import snd.komelia.ui.platform.cursorForHand
import snd.komelia.ui.series.SeriesFilterState
import snd.komelia.ui.series.view.SeriesFilterContent
import snd.komga.client.series.KomgaSeries

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
                SeriesBulkActionsContent(selectedSeries, true)
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
            FULL, EXPANDED -> {
                if (selectedSeries.isEmpty()) {
                    Text("Click on items to select or deselect them")
                } else {
                    Spacer(Modifier.weight(1f))
                    SeriesBulkActionsContent(selectedSeries, false)
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
    Box {
        if (isLoading) {
            LinearProgressIndicator(
                color = MaterialTheme.colorScheme.tertiaryContainer,
                trackColor = Color.Transparent,
                modifier = Modifier.fillMaxWidth().animateContentSize(),
            )
        }
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            var showFilters by remember { mutableStateOf(false) }

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
                modifier = Modifier.fillMaxWidth()
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
}
