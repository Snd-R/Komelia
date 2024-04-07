package io.github.snd_r.komelia.ui.library.view

import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ChevronRight
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.github.snd_r.komelia.platform.VerticalScrollbar
import io.github.snd_r.komelia.platform.cursorForHand
import io.github.snd_r.komelia.ui.common.itemlist.BookCardSlider
import io.github.snd_r.komelia.ui.common.itemlist.SeriesCardSlider
import io.github.snd_r.komelia.ui.common.menus.BookMenuActions
import io.github.snd_r.komelia.ui.common.menus.SeriesMenuActions
import io.github.snd_r.komga.book.KomgaBook
import io.github.snd_r.komga.book.KomgaBookId
import io.github.snd_r.komga.series.KomgaSeries
import io.github.snd_r.komga.series.KomgaSeriesId

@Composable
fun LibraryRecommendedContent(
    keepReadingBooks: List<KomgaBook>,

    recentlyReleasedBooks: List<KomgaBook>,

    recentlyAddedBooks: List<KomgaBook>,

    recentlyAddedSeries: List<KomgaSeries>,
    onRecentlyAddedSeriesClick: () -> Unit,

    recentlyUpdatedSeries: List<KomgaSeries>,
    onRecentlyUpdatedSeriesClick: () -> Unit,

    cardWidth: Dp,
    onSeriesClick: (KomgaSeriesId) -> Unit,
    seriesMenuActions: SeriesMenuActions,
    bookMenuActions: BookMenuActions,
    onBookClick: (KomgaBookId) -> Unit,
    onBookReadClick: (KomgaBookId) -> Unit,
) {
    val scrollState: ScrollState = rememberScrollState()
    Box {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 10.dp)
                .verticalScroll(scrollState),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            BookCardsPanel(
                title = "Keep Reading",
                books = keepReadingBooks,
                cardWidth = cardWidth,
                bookMenuActions = bookMenuActions,
                onBookClick = onBookClick,
                onBookReadClick = onBookReadClick
            )
            BookCardsPanel(
                title = "Recently Released Books",
                books = recentlyReleasedBooks,
                cardWidth = cardWidth,
                bookMenuActions = bookMenuActions,
                onBookClick = onBookClick,
                onBookReadClick = onBookReadClick
            )
            BookCardsPanel(
                title = "Recently Added Books",
                books = recentlyAddedBooks,
                cardWidth = cardWidth,
                bookMenuActions = bookMenuActions,
                onBookClick = onBookClick,
                onBookReadClick = onBookReadClick
            )
            SeriesCardsPanel(
                title = "Recently Added Series",
                series = recentlyAddedSeries,
                cardWidth = cardWidth,
                onClick = onRecentlyAddedSeriesClick,
                onSeriesClick = onSeriesClick,
                seriesActions = seriesMenuActions
            )
            SeriesCardsPanel(
                title = "Recently Updated Series",
                series = recentlyUpdatedSeries,
                cardWidth = cardWidth,
                onClick = onRecentlyUpdatedSeriesClick,
                onSeriesClick = onSeriesClick,
                seriesActions = seriesMenuActions
            )
        }
        VerticalScrollbar(scrollState, Modifier.align(Alignment.CenterEnd))
    }
}

@Composable
fun BookCardsPanel(
    title: String,
    books: List<KomgaBook>,
    cardWidth: Dp,
    bookMenuActions: BookMenuActions,
    onBookClick: (KomgaBookId) -> Unit,
    onBookReadClick: (KomgaBookId) -> Unit,
) {
    if (books.isEmpty()) return
    val scrollState = rememberLazyListState()
    Column {
        CardPanelTop(
            title = title,
            onClick = {},
        )
        BookCardSlider(
            books = books,
            onBookClick = onBookClick,
            cardWidth = cardWidth,
            bookMenuActions = bookMenuActions,
            onBookReadClick = onBookReadClick,
            scrollState = scrollState
        )
    }
}

@Composable
fun SeriesCardsPanel(
    title: String,
    series: List<KomgaSeries>,
    cardWidth: Dp,
    onClick: () -> Unit,
    onSeriesClick: (KomgaSeriesId) -> Unit,
    seriesActions: SeriesMenuActions,
) {
    if (series.isEmpty()) return
    val scrollState = rememberLazyListState()
    Column {
        CardPanelTop(
            title = title,
            onClick = onClick,
        )
        SeriesCardSlider(
            series = series,
            onSeriesClick = onSeriesClick,
            cardWidth = cardWidth,
            seriesActions = seriesActions,
            scrollState = scrollState
        )
    }
}

@Composable
fun CardPanelTop(
    title: String,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick
            )
            .cursorForHand()
            .padding(vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(title, fontSize = 20.sp)

        Spacer(Modifier.width(10.dp))
        Icon(Icons.Rounded.ChevronRight, null)
    }
}



