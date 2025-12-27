package snd.komelia.ui.collection

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.EditNote
import androidx.compose.material.icons.rounded.MoreVert
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import snd.komelia.ui.LocalKomgaState
import snd.komelia.ui.LocalWindowWidth
import snd.komelia.ui.common.components.PageSizeSelectionDropdown
import snd.komelia.ui.common.itemlist.SeriesLazyCardGrid
import snd.komelia.ui.common.menus.CollectionActionsMenu
import snd.komelia.ui.common.menus.SeriesMenuActions
import snd.komelia.ui.common.menus.bulk.BottomPopupBulkActionsPanel
import snd.komelia.ui.common.menus.bulk.BulkActionsContainer
import snd.komelia.ui.common.menus.bulk.CollectionBulkActionsContent
import snd.komelia.ui.platform.WindowSizeClass.COMPACT
import snd.komelia.ui.platform.WindowSizeClass.EXPANDED
import snd.komelia.ui.platform.WindowSizeClass.FULL
import snd.komelia.ui.platform.WindowSizeClass.MEDIUM
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
                CollectionBulkActionsContent(collection, selectedSeries, true)
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

    ) {
    Row(
        modifier = Modifier.padding(start = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {

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

        val isAdmin = LocalKomgaState.current.authenticatedUser.collectAsState().value?.roleAdmin() ?: true
        if (isAdmin) {
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
        }

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
                if (collection.ordered) Text("Click to select, drag to change order")
                else Text("Click on items to select or deselect them")
                if (selectedSeries.isNotEmpty()) {
                    Spacer(Modifier.weight(1f))

                    CollectionBulkActionsContent(collection, selectedSeries, false)
                }
            }

            EXPANDED -> {
                if (selectedSeries.isEmpty()) {
                    if (collection.ordered) Text("Click to select, drag to change order")
                    else Text("Click on items to select or deselect them")
                } else {
                    Spacer(Modifier.weight(1f))
                    CollectionBulkActionsContent(collection, selectedSeries, false)
                }
            }

            COMPACT, MEDIUM -> {}
        }
    }
}