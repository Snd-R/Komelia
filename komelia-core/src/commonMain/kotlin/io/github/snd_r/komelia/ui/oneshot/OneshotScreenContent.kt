package io.github.snd_r.komelia.ui.oneshot

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.FlowRowScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import io.github.snd_r.komelia.platform.VerticalScrollbar
import io.github.snd_r.komelia.platform.WindowSizeClass.COMPACT
import io.github.snd_r.komelia.platform.WindowSizeClass.EXPANDED
import io.github.snd_r.komelia.platform.WindowSizeClass.FULL
import io.github.snd_r.komelia.platform.WindowSizeClass.MEDIUM
import io.github.snd_r.komelia.ui.LocalWindowWidth
import io.github.snd_r.komelia.ui.book.BookInfoColumn
import io.github.snd_r.komelia.ui.book.BookInfoRow
import io.github.snd_r.komelia.ui.collection.SeriesCollectionsContent
import io.github.snd_r.komelia.ui.common.BookReadButton
import io.github.snd_r.komelia.ui.common.ExpandableText
import io.github.snd_r.komelia.ui.common.images.BookThumbnail
import io.github.snd_r.komelia.ui.common.menus.BookMenuActions
import io.github.snd_r.komelia.ui.common.menus.OneshotActionsMenu
import io.github.snd_r.komelia.ui.common.readIsSupported
import io.github.snd_r.komelia.ui.dialogs.oneshot.OneshotEditDialog
import io.github.snd_r.komelia.ui.library.SeriesScreenFilter
import io.github.snd_r.komelia.ui.readlist.BookReadListsContent
import io.github.snd_r.komelia.ui.series.view.SeriesDescriptionRow
import snd.komga.client.book.KomgaBook
import snd.komga.client.collection.KomgaCollection
import snd.komga.client.readlist.KomgaReadList
import snd.komga.client.series.KomgaSeries

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun OneshotScreenContent(
    series: KomgaSeries,
    book: KomgaBook,
    oneshotMenuActions: BookMenuActions,
    onBackButtonClick: () -> Unit,
    onBookReadPress: (markReadProgress: Boolean) -> Unit,

    collections: Map<KomgaCollection, List<KomgaSeries>>,
    onCollectionClick: (KomgaCollection) -> Unit,
    onSeriesClick: (KomgaSeries) -> Unit,

    readLists: Map<KomgaReadList, List<KomgaBook>>,
    onReadListClick: (KomgaReadList) -> Unit,
    onBookClick: (KomgaBook) -> Unit,
    onFilterClick: (SeriesScreenFilter) -> Unit,
    cardWidth: Dp
) {
    val scrollState: ScrollState = rememberScrollState()
    Column(modifier = Modifier.fillMaxSize()) {
        OneshotToolBar(
            series = series,
            book = book,
            bookMenuActions = oneshotMenuActions,
            onBackButtonClick = onBackButtonClick
        )

        val contentPadding = when (LocalWindowWidth.current) {
            COMPACT, MEDIUM -> Modifier.padding(5.dp)
            EXPANDED -> Modifier.padding(start = 20.dp, end = 20.dp)
            FULL -> Modifier.padding(start = 30.dp, end = 30.dp)
        }

        Box {
            Column(
                modifier = contentPadding
                    .fillMaxWidth()
                    .verticalScroll(state = scrollState),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                horizontalAlignment = Alignment.Start
            ) {
                FlowRow(horizontalArrangement = Arrangement.spacedBy(15.dp)) {
                    BookThumbnail(
                        book.id,
                        modifier = Modifier
                            .heightIn(min = 100.dp, max = 400.dp)
                            .widthIn(min = 300.dp, max = 500.dp)
                            .animateContentSize()
                    )
                    OneshotMainInfo(series, book, onBookReadPress)
                }
                BookInfoColumn(
                    publisher = series.metadata.publisher,
                    genres = series.metadata.genres,
                    authors = book.metadata.authors,
                    tags = book.metadata.tags,
                    links = book.metadata.links,
                    sizeInMiB = book.size,
                    mediaType = book.media.mediaType,
                    isbn = book.metadata.isbn,
                    fileUrl = book.url,
                    onFilterClick = onFilterClick
                )
                BookReadListsContent(
                    readLists = readLists,
                    onReadListClick = onReadListClick,
                    onBookClick = onBookClick,
                    cardWidth = cardWidth
                )
                SeriesCollectionsContent(
                    collections = collections,
                    onCollectionClick = onCollectionClick,
                    onSeriesClick = onSeriesClick,
                    cardWidth = cardWidth
                )
            }
            VerticalScrollbar(scrollState, Modifier.align(Alignment.CenterEnd))
        }
    }
}

@Composable
fun OneshotToolBar(
    series: KomgaSeries,
    book: KomgaBook,
    bookMenuActions: BookMenuActions,
    onBackButtonClick: () -> Unit,
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        IconButton(onClick = { onBackButtonClick() }) {
            Icon(Icons.AutoMirrored.Filled.ArrowBack, null)
        }
        Text(
            book.metadata.title,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f, false)
        )
        ToolbarOneshotActions(series, book, bookMenuActions)
    }
}

@Composable
private fun ToolbarOneshotActions(
    series: KomgaSeries,
    book: KomgaBook,
    bookMenuActions: BookMenuActions,
) {
    Row {
        Box {
            var expandActions by remember { mutableStateOf(false) }
            IconButton(onClick = { expandActions = true }) {
                Icon(Icons.Default.MoreVert, contentDescription = null)
            }
            OneshotActionsMenu(
                series = series,
                book = book,
                actions = bookMenuActions,
                expanded = expandActions,
                onDismissRequest = { expandActions = false }
            )
        }

        var showEditDialog by remember { mutableStateOf(false) }
        IconButton(onClick = { showEditDialog = true }) {
            Icon(Icons.Default.Edit, null)
        }
        if (showEditDialog) {
            OneshotEditDialog(
                seriesId = series.id,
                series = series,
                book = book,
                onDismissRequest = { showEditDialog = false })
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun FlowRowScope.OneshotMainInfo(
    series: KomgaSeries,
    book: KomgaBook,
    onBookReadPress: (markReadProgress: Boolean) -> Unit,
) {
    Column(
        modifier = Modifier.weight(1f, false).widthIn(min = 450.dp, max = 1200.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        SeriesDescriptionRow(
            releaseDate = null,
            status = null,
            ageRating = series.metadata.ageRating,
            language = series.metadata.language,
            readingDirection = series.metadata.readingDirection,
            deleted = series.deleted,
            alternateTitles = series.metadata.alternateTitles,
            onFilterClick = {},
            modifier = Modifier.weight(1f, false).widthIn(min = 200.dp),
        )
        BookInfoRow(
            seriesTitle = null,
            readProgress = book.readProgress,
            bookPagesCount = book.media.pagesCount,
            bookNumber = book.metadata.number,
            releaseDate = book.metadata.releaseDate
        )

        if (readIsSupported(book)) {
            BookReadButton(
                onRead = { onBookReadPress(true) },
                onIncognitoRead = { onBookReadPress(false) }
            )
        }
        HorizontalDivider()
        ExpandableText(
            text = book.metadata.summary,
            style = MaterialTheme.typography.bodyMedium
        )
    }
}
