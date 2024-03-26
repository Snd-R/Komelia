package io.github.snd_r.komelia.ui.series.view

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ViewList
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.MoreVert
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.SuggestionChipDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInParent
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.github.snd_r.komelia.platform.VerticalScrollbar
import io.github.snd_r.komelia.platform.cursorForHand
import io.github.snd_r.komelia.ui.common.DescriptionChips
import io.github.snd_r.komelia.ui.common.DropdownChoiceMenu
import io.github.snd_r.komelia.ui.common.ExpandableText
import io.github.snd_r.komelia.ui.common.LoadingMaxSizeIndicator
import io.github.snd_r.komelia.ui.common.LocalWindowSize
import io.github.snd_r.komelia.ui.common.Pagination
import io.github.snd_r.komelia.ui.common.WindowSize
import io.github.snd_r.komelia.ui.common.cards.BookDetailedListCard
import io.github.snd_r.komelia.ui.common.cards.BookImageCard
import io.github.snd_r.komelia.ui.common.cards.ItemCard
import io.github.snd_r.komelia.ui.common.images.SeriesThumbnail
import io.github.snd_r.komelia.ui.common.menus.BookMenuActions
import io.github.snd_r.komelia.ui.common.menus.SeriesActionsMenu
import io.github.snd_r.komelia.ui.common.menus.SeriesMenuActions
import io.github.snd_r.komelia.ui.dialogs.seriesedit.SeriesEditDialog
import io.github.snd_r.komelia.ui.series.BooksLayout
import io.github.snd_r.komelia.ui.series.BooksLayout.GRID
import io.github.snd_r.komelia.ui.series.BooksLayout.LIST
import io.github.snd_r.komga.book.KomgaBook
import io.github.snd_r.komga.book.KomgaBookId
import io.github.snd_r.komga.series.KomgaSeries
import io.github.snd_r.komga.series.KomgaSeriesStatus.ABANDONED
import io.github.snd_r.komga.series.KomgaSeriesStatus.ENDED
import io.github.snd_r.komga.series.KomgaSeriesStatus.HIATUS
import io.github.snd_r.komga.series.KomgaSeriesStatus.ONGOING
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

@Composable
fun SeriesContent(
    series: KomgaSeries?,
    seriesMenuActions: SeriesMenuActions,

    books: List<KomgaBook>,
    booksLoading: Boolean,
    bookCardWidth: Dp,
    booksLayout: BooksLayout,
    onBooksLayoutChange: (BooksLayout) -> Unit,

    booksPageSize: Int,
    onBooksPageSizeChange: (Int) -> Unit,

    bookMenuActions: BookMenuActions,
    totalBookPages: Int,
    currentBookPage: Int,
    onBookClick: (KomgaBookId) -> Unit,
    onBookReadClick: (KomgaBookId) -> Unit,
    onBookPageNumberClick: (Int) -> Unit,
    onBackButtonClick: () -> Unit,
) {
    val contentPadding = when (LocalWindowSize.current) {
        WindowSize.COMPACT, WindowSize.MEDIUM -> Modifier.padding(5.dp)
        WindowSize.EXPANDED -> Modifier.padding(start = 20.dp, end = 20.dp)
        WindowSize.FULL -> Modifier.padding(start = 30.dp, end = 30.dp)
    }

    Column(modifier = Modifier.fillMaxSize()) {
        SeriesToolBar(series, seriesMenuActions, onBackButtonClick)

        if (series == null) {
            LoadingMaxSizeIndicator()
        } else {
            val scrollState = rememberScrollState()
            Box {
                Column(
                    modifier = contentPadding.verticalScroll(scrollState),
                ) {

                    Series(series)

                    HorizontalDivider(Modifier.padding(bottom = 10.dp))

                    Books(
                        series = series,
                        books = books,

                        bookCardWidth = bookCardWidth,
                        booksLoading = booksLoading,
                        booksLayout = booksLayout,
                        onBooksLayoutChange = onBooksLayoutChange,
                        booksPageSize = booksPageSize,
                        onBooksPageSizeChange = onBooksPageSizeChange,

                        bookMenuActions = bookMenuActions,
                        scrollState = scrollState,
                        onBookClick = onBookClick,
                        onBookReadClick = onBookReadClick,
                        totalBookPages = totalBookPages,
                        currentBookPage = currentBookPage,
                        onBookPageNumberClick = onBookPageNumberClick
                    )
                }


                VerticalScrollbar(scrollState, Modifier.align(Alignment.CenterEnd))
            }
        }
    }

}


@Composable
fun SeriesToolBar(
    series: KomgaSeries?,
    seriesMenuActions: SeriesMenuActions,
    onBackButtonClick: () -> Unit,
) {
    Row(verticalAlignment = Alignment.CenterVertically) {

        IconButton(onClick = { onBackButtonClick() }) {
            Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = null)
        }


        if (series != null) {
            Text(
                series.metadata.title,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            Box {
                var expandActions by remember { mutableStateOf(false) }
                IconButton(onClick = { expandActions = true }) {
                    Icon(Icons.Rounded.MoreVert, contentDescription = null)
                }

                SeriesActionsMenu(
                    series = series,
                    actions = seriesMenuActions,
                    expanded = expandActions,
                    showEditOption = false,
                    onDismissRequest = { expandActions = false },
                )
            }

            var showEditDialog by remember { mutableStateOf(false) }
            IconButton(onClick = { showEditDialog = true }) {
                Icon(Icons.Rounded.Edit, contentDescription = null)
            }

            if (showEditDialog)
                SeriesEditDialog(series = series, onDismissRequest = { showEditDialog = false })

        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun Series(
    series: KomgaSeries,
) {
    Column {
        FlowRow {
            SeriesThumbnail(
                seriesId = series.id,
                modifier = Modifier
                    .heightIn(min = 100.dp, max = 400.dp)
                    .widthIn(min = 300.dp, max = 500.dp)
                    .animateContentSize(),
                contentScale = ContentScale.Fit
            )
            SeriesInfo(series, Modifier.weight(1f, false).widthIn(min = 200.dp))
        }
        SeriesInfoLower(series)
    }
}

@Composable
fun SeriesInfo(
    series: KomgaSeries,
    modifier: Modifier
) {
    val contentSize = when (LocalWindowSize.current) {
        WindowSize.COMPACT, WindowSize.MEDIUM -> Modifier.padding(10.dp, 0.dp)
        WindowSize.EXPANDED -> Modifier.padding(20.dp, 0.dp).fillMaxSize()
        WindowSize.FULL -> Modifier.padding(30.dp, 0.dp).fillMaxSize(0.7f)
    }

    Column(
        modifier = contentSize.then(modifier),
        verticalArrangement = Arrangement.spacedBy(5.dp),
        horizontalAlignment = Alignment.Start
    ) {

        val releaseDate = series.booksMetadata.releaseDate
        if (releaseDate != null)
            Text("Release Year: ${releaseDate.year}", fontSize = 10.sp)

        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            SuggestionChip(
                onClick = {},
                label = { Text(series.metadata.status.name) },
                border = null,
                colors = SuggestionChipDefaults.suggestionChipColors(
                    containerColor = when (series.metadata.status) {
                        ENDED -> MaterialTheme.colorScheme.secondary
                        ONGOING -> MaterialTheme.colorScheme.surfaceVariant
                        ABANDONED -> MaterialTheme.colorScheme.errorContainer
                        HIATUS -> MaterialTheme.colorScheme.tertiaryContainer
                    },
                )
            )

            series.metadata.ageRating?.let {
                SuggestionChip(
                    onClick = {},
                    label = { Text("$it+") }
                )
            }

            if (series.metadata.language.isNotBlank())
                SuggestionChip(
                    onClick = {},
                    label = { Text(series.metadata.language) }
                )

            val readingDirection = series.metadata.readingDirection
            if (readingDirection != null) {
                SuggestionChip(
                    onClick = {},
                    label = { Text(readingDirection.name) }
                )
            }

            if (series.deleted) {
                SuggestionChip(
                    onClick = {},
                    label = { Text("Unavailable") },
                    border = null,
                    colors = SuggestionChipDefaults.suggestionChipColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer
                    )
                )
            }
        }

        if (series.metadata.alternateTitles.isNotEmpty()) {
            Column {
                Text("Alternative titles", fontWeight = FontWeight.Bold)
                series.metadata.alternateTitles.forEach {
                    Row {
                        Text(
                            it.label,
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.widthIn(min = 100.dp, max = 200.dp)
                        )
                        Text(
                            it.title,
                            style = MaterialTheme.typography.bodyMedium,
                        )
                    }
                }
            }
            HorizontalDivider(Modifier.padding(vertical = 10.dp))
        }


        ExpandableText(
            series.metadata.summary,
            style = MaterialTheme.typography.bodyMedium
        )
    }
}

@Composable
fun SeriesInfoLower(series: KomgaSeries) {
    Column(
        modifier = Modifier.padding(0.dp, 30.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        DescriptionChips("PUBLISHER", series.metadata.publisher)
        DescriptionChips("GENRES", series.metadata.genres)
        DescriptionChips("TAGS", series.metadata.tags, series.booksMetadata.tags)

        val uriHandler = LocalUriHandler.current
        DescriptionChips(
            label = "LINKS",
            chipValues = series.metadata.links.map { it.label },
            onChipClick = { chip ->
                series.metadata.links
                    .firstOrNull { it.label == chip }
                    ?.let { uriHandler.openUri(it.url) }
            }
        )

        Spacer(Modifier.height(10.dp))

        series.booksMetadata.authors
            .filter { it.role == "writer" }
            .groupBy { it.role }
            .forEach { (_, author) -> DescriptionChips("WRITERS", author.map { it.name }) }

        series.booksMetadata.authors
            .filter { it.role == "penciller" }
            .groupBy { it.role }
            .forEach { (_, author) -> DescriptionChips("PENCILLERS", author.map { it.name }) }
    }

}

@Composable
fun Books(
    series: KomgaSeries,
    books: List<KomgaBook>,

    bookCardWidth: Dp,
    booksLoading: Boolean,

    booksLayout: BooksLayout,
    onBooksLayoutChange: (BooksLayout) -> Unit,
    booksPageSize: Int,
    onBooksPageSizeChange: (Int) -> Unit,

    scrollState: ScrollState,
    bookMenuActions: BookMenuActions,
    onBookClick: (KomgaBookId) -> Unit,
    onBookReadClick: (KomgaBookId) -> Unit,
    totalBookPages: Int,
    currentBookPage: Int,
    onBookPageNumberClick: (Int) -> Unit,
) {

    var scrollToPosition by remember { mutableStateOf(0f) }
    Column(Modifier.onGloballyPositioned { scrollToPosition = it.positionInParent().y }) {
        BooksToolBar(
            series = series,
            booksLayout = booksLayout,
            onBooksLayoutChange = onBooksLayoutChange,
            booksPageSize = booksPageSize,
            onBooksPageSizeChange = onBooksPageSizeChange,
            totalBookPages = totalBookPages,
            currentBookPage = currentBookPage,
            onBookPageNumberClick = onBookPageNumberClick
        )

        when (booksLayout) {
            GRID -> {
                BooksGrid(
                    books = books,
                    cardWidth = bookCardWidth,
                    bookMenuActions = bookMenuActions,
                    onBookClick = onBookClick,
                    onBookReadClick = onBookReadClick,
                    loadPlaceholder = {
                        for (i in 0 until booksPageSize) {
                            ItemCard(Modifier.width(bookCardWidth), onClick = {}, image = {})
                        }
                    },
                    isLoading = booksLoading,
                )
            }

            LIST -> BooksList(
                books = books,
                bookMenuActions = bookMenuActions,
                onBookClick = onBookClick,
                onBookReadClick = onBookReadClick,
                isLoading = booksLoading,
            )
        }


        val coroutineScope = rememberCoroutineScope()
        Pagination(
            totalPages = totalBookPages,
            currentPage = currentBookPage,
            onPageChange = {
                onBookPageNumberClick(it)
                coroutineScope.launch { scrollState.animateScrollTo(scrollToPosition.roundToInt()) }
            },
            modifier = Modifier.fillMaxSize()
        )
    }
}

@Composable
private fun BooksToolBar(
    series: KomgaSeries,

    booksLayout: BooksLayout,
    onBooksLayoutChange: (BooksLayout) -> Unit,
    booksPageSize: Int,
    onBooksPageSizeChange: (Int) -> Unit,

    totalBookPages: Int,
    currentBookPage: Int,
    onBookPageNumberClick: (Int) -> Unit,
) {

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(vertical = 10.dp)
    ) {
        val booksLabel = buildString {
            append(series.booksCount)
            if (series.metadata.totalBookCount != null) append(" / ${series.metadata.totalBookCount}")
            if (series.booksCount > 1) append(" books")
            else append(" book")
        }
        SuggestionChip(
            onClick = {},
            label = { Text(booksLabel, style = MaterialTheme.typography.bodyMedium) },
            modifier = Modifier.padding(10.dp, 0.dp)
        )
        Pagination(
            totalPages = totalBookPages,
            currentPage = currentBookPage,
            onPageChange = onBookPageNumberClick,
            modifier = Modifier.weight(1f)
        )

        DropdownChoiceMenu(
            selectedOption = booksPageSize,
            options = listOf(20, 50, 100, 200, 500),
            onOptionChange = onBooksPageSizeChange,
            label = {},
            modifier = Modifier.width(70.dp)
        )

        Row {
            Box(Modifier
                .background(if (booksLayout == LIST) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.surface)
                .clickable { onBooksLayoutChange(LIST) }
                .cursorForHand()
                .padding(10.dp)
            ) {
                Icon(Icons.AutoMirrored.Filled.ViewList, null)
            }

            Box(Modifier
                .background(if (booksLayout == GRID) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.surface)
                .clickable { onBooksLayoutChange(GRID) }
                .cursorForHand()
                .padding(10.dp)
            ) {
                Icon(Icons.Default.GridView, null)
            }
        }
    }

}


@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun BooksGrid(
    books: List<KomgaBook>,
    cardWidth: Dp,
    bookMenuActions: BookMenuActions,
    onBookClick: (KomgaBookId) -> Unit,
    onBookReadClick: (KomgaBookId) -> Unit,

    loadPlaceholder: @Composable () -> Unit,
    isLoading: Boolean,
) {
    FlowRow(
        horizontalArrangement = Arrangement.spacedBy(20.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp),
        modifier = Modifier.padding(20.dp)
    ) {
        if (isLoading) {
            loadPlaceholder()
        } else {
            books.forEach {
                BookImageCard(
                    book = it,
                    onBookClick = { onBookClick(it.id) },
                    onBookReadClick = { onBookReadClick(it.id) },
                    bookMenuActions = bookMenuActions,
                    modifier = Modifier.width(cardWidth)
                )

            }
        }
    }
}

@Composable
private fun BooksList(
    books: List<KomgaBook>,
    bookMenuActions: BookMenuActions,
    onBookClick: (KomgaBookId) -> Unit,
    onBookReadClick: (KomgaBookId) -> Unit,

    isLoading: Boolean,
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(10.dp),
        horizontalAlignment = Alignment.Start,
    ) {

        if (!isLoading) {
            books.forEach { book ->
                BookDetailedListCard(
                    book = book,
                    onClick = { onBookClick(book.id) },
                    bookMenuActions = bookMenuActions,
                    onBookReadClick = { onBookReadClick(book.id) },
                )
            }
        }
    }
}