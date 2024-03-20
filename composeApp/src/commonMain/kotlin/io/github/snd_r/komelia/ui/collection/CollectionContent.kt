package io.github.snd_r.komelia.ui.collection

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
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
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import io.github.snd_r.komelia.ui.common.PaginationWithSizeOptions
import io.github.snd_r.komelia.ui.common.itemlist.SeriesLazyCardGrid
import io.github.snd_r.komelia.ui.common.menus.CollectionActionsMenu
import io.github.snd_r.komelia.ui.common.menus.SeriesMenuActions
import io.github.snd_r.komga.collection.KomgaCollection
import io.github.snd_r.komga.series.KomgaSeries
import io.github.snd_r.komga.series.KomgaSeriesId

@Composable
fun CollectionContent(
    collection: KomgaCollection,
    onCollectionDelete: () -> Unit,

    series: List<KomgaSeries>,
    totalSeriesCount: Int,
    seriesActions: SeriesMenuActions,
    onSeriesClick: (KomgaSeriesId) -> Unit,

    totalPages: Int,
    currentPage: Int,
    pageSize: Int,
    onPageChange: (Int) -> Unit,
    onPageSizeChange: (Int) -> Unit,

    onBackClick: () -> Unit,
    cardMinSize: Dp,
) {
    Column {
        CollectionToolbar(
            collection = collection,
            onCollectionDelete = onCollectionDelete,

            totalSeriesCount = totalSeriesCount,
            totalPages = totalPages,
            currentPage = currentPage,
            pageSize = pageSize,
            onPageChange = onPageChange,
            onPageSizeChange = onPageSizeChange,

            onBackClick = onBackClick
        )

        SeriesLazyCardGrid(
            series = series,
            seriesMenuActions = seriesActions,
            minSize = cardMinSize,
            onSeriesClick = onSeriesClick
        )
    }
}

@Composable
private fun CollectionToolbar(
    collection: KomgaCollection,
    onCollectionDelete: () -> Unit,
    onBackClick: () -> Unit,

    totalSeriesCount: Int,
    totalPages: Int,
    currentPage: Int,
    pageSize: Int,
    onPageChange: (Int) -> Unit,
    onPageSizeChange: (Int) -> Unit,
) {
    Row(verticalAlignment = Alignment.CenterVertically) {

        IconButton(onClick = { onBackClick() }) {
            Icon(Icons.AutoMirrored.Rounded.ArrowBack, null)
        }

        Text(collection.name)

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

        SuggestionChip(
            onClick = {},
            label = { Text("$totalSeriesCount series", style = MaterialTheme.typography.bodyMedium) },
            modifier = Modifier.padding(horizontal = 10.dp),
        )

        PaginationWithSizeOptions(
            totalPages = totalPages,
            currentPage = currentPage,
            onPageChange = onPageChange,
            navigationButtons = false,
            pageSize = pageSize,
            onPageSizeChange = onPageSizeChange,
            spacer = { Spacer(Modifier.weight(1f)) }
        )

    }

}