package io.github.snd_r.komelia.ui.series.view

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import io.github.snd_r.komelia.ui.common.DropdownChoiceMenu
import io.github.snd_r.komelia.ui.common.PaginationWithSizeOptions
import io.github.snd_r.komelia.ui.common.itemlist.PlaceHolderLazyCardGrid
import io.github.snd_r.komelia.ui.common.itemlist.SeriesLazyCardGrid
import io.github.snd_r.komelia.ui.common.menus.SeriesMenuActions
import io.github.snd_r.komga.series.KomgaSeries
import io.github.snd_r.komga.series.KomgaSeriesId
import io.github.snd_r.komga.series.KomgaSeriesSort

@Composable
fun SeriesListContent(
    series: List<KomgaSeries>,
    seriesTotalCount: Int,
    seriesActions: SeriesMenuActions,
    onSeriesClick: (KomgaSeriesId) -> Unit,
    isLoading: Boolean,

    sortOrder: KomgaSeriesSort,
    onSortOrderChange: (KomgaSeriesSort) -> Unit,

    totalPages: Int,
    currentPage: Int,
    pageSize: Int,
    onPageChange: (Int) -> Unit,
    onPageSizeChange: (Int) -> Unit,

    minSize: Dp,
) {
    Column(verticalArrangement = Arrangement.Center) {
        ToolBar(
            seriesTotalCount = seriesTotalCount,
            sortOrder = sortOrder,
            onSortOrderChange = onSortOrderChange,
            totalPages = totalPages,
            currentPage = currentPage,
            pageSize = pageSize,
            onPageChange = onPageChange,
            onPageSizeChange = onPageSizeChange
        )


        if (isLoading) {
            PlaceHolderLazyCardGrid(
                elements = pageSize,
                minSize = minSize,
            )
        } else {
            SeriesLazyCardGrid(
                series = series,
                seriesMenuActions = seriesActions,
                minSize = minSize,
                onSeriesClick = onSeriesClick
            )
        }
    }
}

@Composable
private fun ToolBar(
    seriesTotalCount: Int,
    sortOrder: KomgaSeriesSort,
    onSortOrderChange: (KomgaSeriesSort) -> Unit,

    totalPages: Int,
    currentPage: Int,
    pageSize: Int,
    onPageChange: (Int) -> Unit,
    onPageSizeChange: (Int) -> Unit,
) {

    Row(
        horizontalArrangement = Arrangement.End,
        modifier = Modifier.fillMaxWidth().padding(horizontal = 10.dp)
    ) {
        SuggestionChip(
            onClick = {},
            label = { Text("$seriesTotalCount series") },
            modifier = Modifier.padding(end = 10.dp)
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

        DropdownChoiceMenu(
            selectedOption = sortOrder,
            options = KomgaSeriesSort.entries,
            onOptionChange = onSortOrderChange,
            inputFieldColor = MaterialTheme.colorScheme.surfaceVariant,
            contentPadding = PaddingValues(5.dp),
            modifier = Modifier
                .widthIn(min = 200.dp)
                .clip(RoundedCornerShape(5.dp))
        )
    }
}