package io.github.snd_r.komelia.ui.series.view

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.Card
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import io.github.snd_r.komelia.platform.HorizontalScrollbar
import io.github.snd_r.komelia.platform.cursorForHand
import io.github.snd_r.komelia.ui.common.cards.SeriesImageCard
import io.github.snd_r.komelia.ui.series.SeriesCollectionsState
import io.github.snd_r.komga.collection.KomgaCollection
import io.github.snd_r.komga.series.KomgaSeries

@Composable
fun SeriesCollectionsContent(
    state: SeriesCollectionsState,
    onCollectionClick: (KomgaCollection) -> Unit,
    onSeriesClick: (KomgaSeries) -> Unit,
) {
    Column(
        modifier = Modifier.padding(top = 10.dp, bottom = 30.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        state.collections.forEach { (collection, series) ->
            CollectionCardSlider(
                collection = collection,
                onCollectionClick = { onCollectionClick(collection) },
                series = series,
                onSeriesClick = onSeriesClick,
                cardWidth = state.cardWidth.collectAsState().value,
            )
        }
    }

}

@Composable
private fun CollectionCardSlider(
    collection: KomgaCollection,
    onCollectionClick: () -> Unit,
    series: List<KomgaSeries>,
    onSeriesClick: (KomgaSeries) -> Unit,
    cardWidth: Dp = 250.dp,
) {
    val scrollState = rememberLazyListState()
    Card {
        Column(
            Modifier.padding(15.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {

            Row(
                Modifier
                    .fillMaxWidth()
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = onCollectionClick
                    ).cursorForHand()
            ) {
                Text(
                    buildAnnotatedString {
                        withStyle(SpanStyle(fontStyle = FontStyle.Italic)) {
                            append("collection ")
                        }
                        append(collection.name)
                    },
                    style = MaterialTheme.typography.titleMedium,
                    textDecoration = TextDecoration.Underline
                )
                Icon(Icons.Default.ChevronRight, null)
            }
            HorizontalDivider()
            LazyRow(
                state = scrollState,
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                items(series) { series ->

                    SeriesImageCard(
                        series = series,
                        onSeriesClick = { onSeriesClick(series) },
                        modifier = Modifier.width(cardWidth)
                    )

                }
            }
            HorizontalScrollbar(scrollState, Modifier.align(Alignment.End).height(10.dp))
        }
    }
}
