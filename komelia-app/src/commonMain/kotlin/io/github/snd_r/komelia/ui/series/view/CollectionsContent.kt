package io.github.snd_r.komelia.ui.series.view

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import io.github.snd_r.komelia.ui.common.cards.SeriesImageCard
import io.github.snd_r.komelia.ui.common.itemlist.ItemCardsSlider
import io.github.snd_r.komelia.ui.series.SeriesCollectionsState
import snd.komga.client.collection.KomgaCollection
import snd.komga.client.series.KomgaSeries

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
            ItemCardsSlider(
                onClick = { onCollectionClick(collection) },
                label = { CollectionLabel(collection) }
            ) {
                items(series) { series ->
                    SeriesImageCard(
                        series = series,
                        onSeriesClick = { onSeriesClick(series) },
                        modifier = Modifier.width(state.cardWidth.collectAsState().value)
                    )
                }

            }
        }
    }
}

@Composable
private fun CollectionLabel(collection: KomgaCollection) {
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
}
