package io.github.snd_r.komelia.ui.series.view

import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.FiniteAnimationSpec
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
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
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.github.snd_r.komelia.platform.VerticalScrollbar
import io.github.snd_r.komelia.platform.WindowWidth.COMPACT
import io.github.snd_r.komelia.platform.WindowWidth.EXPANDED
import io.github.snd_r.komelia.platform.WindowWidth.FULL
import io.github.snd_r.komelia.platform.WindowWidth.MEDIUM
import io.github.snd_r.komelia.platform.cursorForHand
import io.github.snd_r.komelia.ui.LocalWindowWidth
import io.github.snd_r.komelia.ui.common.DescriptionChips
import io.github.snd_r.komelia.ui.common.ExpandableText
import io.github.snd_r.komelia.ui.common.LabeledEntry
import io.github.snd_r.komelia.ui.common.LabeledEntry.Companion.stringEntry
import io.github.snd_r.komelia.ui.common.LoadingMaxSizeIndicator
import io.github.snd_r.komelia.ui.common.PageSizeSelectionDropdown
import io.github.snd_r.komelia.ui.common.Pagination
import io.github.snd_r.komelia.ui.common.cards.ItemCard
import io.github.snd_r.komelia.ui.common.images.SeriesThumbnail
import io.github.snd_r.komelia.ui.common.itemlist.BooksGrid
import io.github.snd_r.komelia.ui.common.itemlist.BooksList
import io.github.snd_r.komelia.ui.common.menus.BookMenuActions
import io.github.snd_r.komelia.ui.common.menus.SeriesActionsMenu
import io.github.snd_r.komelia.ui.common.menus.SeriesMenuActions
import io.github.snd_r.komelia.ui.dialogs.series.edit.SeriesEditDialog
import io.github.snd_r.komelia.ui.library.SeriesTabFilter
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

    onFilterClick: (SeriesTabFilter) -> Unit,

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
    val windowWidth = LocalWindowWidth.current
    val contentPadding = when (windowWidth) {
        COMPACT, MEDIUM -> Modifier.padding(5.dp)
        EXPANDED -> Modifier.padding(start = 20.dp, end = 20.dp)
        FULL -> Modifier.padding(start = 30.dp, end = 30.dp)
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

                    Series(series, onFilterClick)

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
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f, false)
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
    onFilterClick: (SeriesTabFilter) -> Unit,
) {
    val width = LocalWindowWidth.current
    val animation: FiniteAnimationSpec<IntSize> = remember(series) {
        when (width) {
            COMPACT, MEDIUM -> spring(stiffness = Spring.StiffnessHigh)
            else -> spring(stiffness = Spring.StiffnessMediumLow)
        }
    }

    Column {
        FlowRow {
            SeriesThumbnail(
                seriesId = series.id,
                modifier = Modifier
                    .animateContentSize(animationSpec = animation)
                    .heightIn(min = 100.dp, max = 400.dp)
                    .widthIn(min = 300.dp, max = 500.dp),
                contentScale = ContentScale.Fit
            )

            SeriesInfo(series, onFilterClick, Modifier.weight(1f, false).widthIn(min = 200.dp))
        }
        SeriesInfoLower(series, onFilterClick)
    }
}

@Composable
fun SeriesInfo(
    series: KomgaSeries,
    onFilterClick: (SeriesTabFilter) -> Unit,
    modifier: Modifier
) {
    val contentSize = when (LocalWindowWidth.current) {
        COMPACT, MEDIUM -> Modifier.padding(10.dp, 0.dp)
        EXPANDED -> Modifier.padding(10.dp, 0.dp)
        FULL -> Modifier.padding(30.dp, 0.dp).fillMaxSize(0.8f)
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
                onClick = { onFilterClick(SeriesTabFilter(publicationStatus = listOf(series.metadata.status))) },
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

            series.metadata.ageRating?.let { age ->
                SuggestionChip(
                    onClick = { onFilterClick(SeriesTabFilter(ageRating = listOf(age))) },
                    label = { Text("$age+") }
                )
            }

            if (series.metadata.language.isNotBlank())
                SuggestionChip(
                    onClick = { onFilterClick(SeriesTabFilter(language = listOf(series.metadata.language))) },
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
fun SeriesInfoLower(
    series: KomgaSeries,
    onFilterClick: (SeriesTabFilter) -> Unit,
) {
    Column(
        modifier = Modifier.padding(0.dp, 30.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        if (series.metadata.publisher.isNotBlank()) {
            DescriptionChips(
                label = "PUBLISHER",
                chipValue = stringEntry(series.metadata.publisher),
                onClick = { onFilterClick(SeriesTabFilter(publisher = listOf(it))) }
            )
        }
        DescriptionChips(
            label = "GENRES",
            chipValues = series.metadata.genres.map { stringEntry(it) },
            onChipClick = { onFilterClick(SeriesTabFilter(genres = listOf(it))) }
        )
        DescriptionChips(
            label = "TAGS",
            chipValues = series.metadata.tags.map { stringEntry(it) },
            secondaryValues = series.booksMetadata.tags.map { stringEntry(it) },
            onChipClick = { onFilterClick(SeriesTabFilter(tags = listOf(it))) }
        )

        val uriHandler = LocalUriHandler.current
        DescriptionChips(
            label = "LINKS",
            chipValues = series.metadata.links.map { LabeledEntry(it, it.label) },
            onChipClick = { entry -> uriHandler.openUri(entry.url) }
        )

        Spacer(Modifier.height(10.dp))

        series.booksMetadata.authors
            .filter { it.role == "writer" }
            .groupBy { it.role }
            .forEach { (_, author) ->
                DescriptionChips(
                    label = "WRITERS",
                    chipValues = author.map { LabeledEntry(it, it.name) },
                    onChipClick = { onFilterClick(SeriesTabFilter(authors = listOf(it))) }
                )
            }

        series.booksMetadata.authors
            .filter { it.role == "penciller" }
            .groupBy { it.role }
            .forEach { (_, author) ->
                DescriptionChips(
                    label = "PENCILLERS",
                    chipValues = author.map { LabeledEntry(it, it.name) },
                    onChipClick = { onFilterClick(SeriesTabFilter(authors = listOf(it))) }
                )
            }
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
    val width = LocalWindowWidth.current
    val alignment = remember(width) {
        when (width) {
            COMPACT -> Alignment.CenterHorizontally
            else -> Alignment.Start
        }
    }

    var scrollToPosition by remember { mutableStateOf(0f) }
    Column(
        modifier = Modifier.onGloballyPositioned { scrollToPosition = it.positionInParent().y },
        horizontalAlignment = alignment
    ) {
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
    val width = LocalWindowWidth.current
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(bottom = 5.dp)
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
            when (width) {
                EXPANDED, FULL -> Pagination(
                    totalPages = totalBookPages,
                    currentPage = currentBookPage,
                    onPageChange = onBookPageNumberClick,
                    modifier = Modifier.weight(1f)
                )

                else -> {
                    Spacer(Modifier.weight(1f))
                }
            }

            PageSizeSelectionDropdown(booksPageSize, onBooksPageSizeChange)

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

        when (width) {
            COMPACT, MEDIUM -> Pagination(
                totalPages = totalBookPages,
                currentPage = currentBookPage,
                onPageChange = onBookPageNumberClick,
            )

            else -> {}
        }
    }

}

