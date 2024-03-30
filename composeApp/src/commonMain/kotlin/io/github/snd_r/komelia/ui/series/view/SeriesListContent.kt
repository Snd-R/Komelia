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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import io.github.snd_r.komelia.ui.common.DropdownChoiceMenu
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
            pageSize = pageSize,
            onPageSizeChange = onPageSizeChange,
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
                onSeriesClick = onSeriesClick,
                totalPages = totalPages,
                currentPage = currentPage,
                onPageChange = onPageChange
            )
        }
    }
}

@Composable
private fun ToolBar(
    seriesTotalCount: Int,
    pageSize: Int,
    onPageSizeChange: (Int) -> Unit,
) {
    Row(
        horizontalArrangement = Arrangement.End,
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth().padding(horizontal = 10.dp)
    ) {
        SuggestionChip(
            onClick = {},
            label = { Text("$seriesTotalCount series") },
        )

        Spacer(Modifier.weight(1f))

        DropdownChoiceMenu(
            selectedOption = pageSize,
            options = listOf(20, 50, 100, 200, 500),
            onOptionChange = onPageSizeChange,
            contentPadding = PaddingValues(5.dp),
            modifier = Modifier
                .widthIn(min = 70.dp)
                .clip(RoundedCornerShape(5.dp))
                .padding(end = 10.dp)
        )

        IconButton(onClick = {}) {
            Icon(Icons.Default.FilterList, null)
        }
    }
}