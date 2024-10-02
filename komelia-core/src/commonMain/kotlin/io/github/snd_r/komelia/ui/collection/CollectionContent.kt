package io.github.snd_r.komelia.ui.collection

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.filled.EditNote
import androidx.compose.material.icons.rounded.MoreVert
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import io.github.snd_r.komelia.platform.WindowWidth.COMPACT
import io.github.snd_r.komelia.platform.WindowWidth.EXPANDED
import io.github.snd_r.komelia.platform.WindowWidth.FULL
import io.github.snd_r.komelia.platform.WindowWidth.MEDIUM
import io.github.snd_r.komelia.ui.LocalWindowWidth
import io.github.snd_r.komelia.ui.common.PageSizeSelectionDropdown
import io.github.snd_r.komelia.ui.common.itemlist.SeriesLazyCardGrid
import io.github.snd_r.komelia.ui.common.menus.CollectionActionsMenu
import io.github.snd_r.komelia.ui.common.menus.SeriesMenuActions
import io.github.snd_r.komelia.ui.common.menus.bulk.BottomPopupBulkActionsPanel
import io.github.snd_r.komelia.ui.common.menus.bulk.BulkActionsContainer
import io.github.snd_r.komelia.ui.common.menus.bulk.CollectionBulkActionsContent
import io.github.snd_r.komelia.ui.common.menus.bulk.SeriesBulkActionsContent
import snd.komga.client.collection.KomgaCollection
import snd.komga.client.series.KomgaSeries

@Composable
fun CollectionContent(
    collection: KomgaCollection,
    onCollectionDelete: () -> Unit,

    series: List<KomgaSeries>,
    totalSeriesCount: Int,

    editMode: Boolean,
    onEditModeChange: (Boolean) -> Unit,
    onSeriesClick: (KomgaSeries) -> Unit,
    seriesActions: SeriesMenuActions,
    onReorder: (fromIndex: Int, toIndex: Int) -> Unit,
    onReorderDragStateChange: (dragging: Boolean) -> Unit = {},

    selectedSeries: List<KomgaSeries>,
    onSeriesSelect: (KomgaSeries) -> Unit,

    totalPages: Int,
    currentPage: Int,
    pageSize: Int,
    onPageChange: (Int) -> Unit,
    onPageSizeChange: (Int) -> Unit,

    onBackClick: () -> Unit,
    cardMinSize: Dp,
) {
    Column {
        if (editMode) {
            BulkActionsToolbar(
                onCancel = { onEditModeChange(false) },
                collection = collection,
                series = series,
                selectedSeries = selectedSeries,
                onSeriesSelect = onSeriesSelect
            )
        } else CollectionToolbar(
            collection = collection,
            onCollectionDelete = onCollectionDelete,
            onEditModeEnable = { onEditModeChange(true) },

            totalSeriesCount = totalSeriesCount,
            pageSize = pageSize,
            onPageSizeChange = onPageSizeChange,

            onBackClick = onBackClick
        )

        SeriesLazyCardGrid(
            series = series,
            onSeriesClick = if (editMode) onSeriesSelect else onSeriesClick,
            seriesMenuActions = if (editMode) null else seriesActions,

            selectedSeries = selectedSeries,
            onSeriesSelect = onSeriesSelect,

            reorderable = collection.ordered && editMode,
            onReorder = onReorder,
            onReorderDragStateChange = onReorderDragStateChange,

            totalPages = totalPages,
            currentPage = currentPage,
            onPageChange = onPageChange,

            minSize = cardMinSize,
            modifier = Modifier.weight(1f)
        )

        val width = LocalWindowWidth.current
        if ((width == COMPACT || width == MEDIUM) && selectedSeries.isNotEmpty()) {
            BottomPopupBulkActionsPanel {
                CollectionBulkActionsContent(collection, selectedSeries, false)
                SeriesBulkActionsContent(selectedSeries, false)
            }
        }
    }
}


@Composable
private fun CollectionToolbar(
    collection: KomgaCollection,
    onCollectionDelete: () -> Unit,
    onEditModeEnable: () -> Unit,

    totalSeriesCount: Int,
    pageSize: Int,
    onPageSizeChange: (Int) -> Unit,

    onBackClick: () -> Unit,
) {
    Row(verticalAlignment = Alignment.CenterVertically) {

        IconButton(onClick = { onBackClick() }) {
            Icon(Icons.AutoMirrored.Rounded.ArrowBack, null)
        }

        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                "collection",
                style = MaterialTheme.typography.labelMedium,
                fontStyle = FontStyle.Italic
            )
            Spacer(Modifier.width(5.dp))
            Text(collection.name, style = MaterialTheme.typography.titleMedium)
        }
        SuggestionChip(
            onClick = {},
            label = { Text("$totalSeriesCount series", style = MaterialTheme.typography.bodyMedium) },
            modifier = Modifier.padding(horizontal = 10.dp),
        )

        Box {
            var expandActions by remember { mutableStateOf(false) }
            IconButton(onClick = { expandActions = true }) {
                Icon(Icons.Rounded.MoreVert, null)
            }

            CollectionActionsMenu(
                collection = collection,
                onCollectionDelete = onCollectionDelete,
                expanded = expandActions,
                onDismissRequest = { expandActions = false }
            )
        }

        IconButton(onClick = onEditModeEnable) { Icon(Icons.Default.EditNote, null) }

        Spacer(Modifier.weight(1f))
        PageSizeSelectionDropdown(pageSize, onPageSizeChange)
    }
}

@Composable
private fun BulkActionsToolbar(
    onCancel: () -> Unit,
    collection: KomgaCollection,
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
                if (collection.ordered) Text("Edit mode: Click to select, drag to change order")
                else Text("Selection mode: Click on items to select or deselect them")
                if (selectedSeries.isNotEmpty()) {
                    Spacer(Modifier.weight(1f))

                    CollectionBulkActionsContent(collection, selectedSeries, true)
                    SeriesBulkActionsContent(selectedSeries, true)
                }
            }

            EXPANDED -> {
                if (selectedSeries.isEmpty()) {
                    if (collection.ordered) Text("Edit mode: Click to select, drag to change order")
                    else Text("Selection mode: Click on items to select or deselect them")
                } else {
                    Spacer(Modifier.weight(1f))
                    CollectionBulkActionsContent(collection, selectedSeries, true)
                    SeriesBulkActionsContent(selectedSeries, true)
                }
            }

            COMPACT, MEDIUM -> {}
        }
    }
}