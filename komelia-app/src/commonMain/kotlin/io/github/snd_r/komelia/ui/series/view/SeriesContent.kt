package io.github.snd_r.komelia.ui.series.view

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.FiniteAnimationSpec
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
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
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.MoreVert
import androidx.compose.material3.FilterChip
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
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.github.snd_r.komelia.platform.VerticalScrollbar
import io.github.snd_r.komelia.platform.WindowWidth.COMPACT
import io.github.snd_r.komelia.platform.WindowWidth.EXPANDED
import io.github.snd_r.komelia.platform.WindowWidth.FULL
import io.github.snd_r.komelia.platform.WindowWidth.MEDIUM
import io.github.snd_r.komelia.ui.LocalWindowWidth
import io.github.snd_r.komelia.ui.common.AppFilterChipDefaults
import io.github.snd_r.komelia.ui.common.DescriptionChips
import io.github.snd_r.komelia.ui.common.ExpandableText
import io.github.snd_r.komelia.ui.common.LabeledEntry
import io.github.snd_r.komelia.ui.common.LabeledEntry.Companion.stringEntry
import io.github.snd_r.komelia.ui.common.images.SeriesThumbnail
import io.github.snd_r.komelia.ui.common.menus.SeriesActionsMenu
import io.github.snd_r.komelia.ui.common.menus.SeriesMenuActions
import io.github.snd_r.komelia.ui.dialogs.series.edit.SeriesEditDialog
import io.github.snd_r.komelia.ui.library.SeriesScreenFilter
import io.github.snd_r.komelia.ui.series.SeriesBooksState
import io.github.snd_r.komelia.ui.series.SeriesCollectionsState
import io.github.snd_r.komelia.ui.series.SeriesViewModel.SeriesTab
import snd.komga.client.book.KomgaBook
import snd.komga.client.collection.KomgaCollection
import snd.komga.client.series.KomgaSeries
import snd.komga.client.series.KomgaSeriesStatus.ABANDONED
import snd.komga.client.series.KomgaSeriesStatus.ENDED
import snd.komga.client.series.KomgaSeriesStatus.HIATUS
import snd.komga.client.series.KomgaSeriesStatus.ONGOING

@Composable
fun SeriesContent(
    series: KomgaSeries?,
    seriesMenuActions: SeriesMenuActions,
    onFilterClick: (SeriesScreenFilter) -> Unit,

    currentTab: SeriesTab,
    onTabChange: (SeriesTab) -> Unit,

    booksState: SeriesBooksState,
    onBookClick: (KomgaBook) -> Unit,
    onBookReadClick: (KomgaBook) -> Unit,

    collectionsState: SeriesCollectionsState,
    onCollectionClick: (KomgaCollection) -> Unit,
    onSeriesClick: (KomgaSeries) -> Unit,

    onBackButtonClick: () -> Unit,
) {
    val windowWidth = LocalWindowWidth.current
    val contentPadding = when (windowWidth) {
        COMPACT, MEDIUM -> Modifier.padding(5.dp)
        EXPANDED -> Modifier.padding(start = 20.dp, end = 20.dp)
        FULL -> Modifier.padding(start = 30.dp, end = 30.dp)
    }

    Column(modifier = Modifier.fillMaxSize()) {
        if (booksState.booksSelectionMode) {
            BooksBulkActionsToolbar(
                onCancel = { booksState.setSelectionMode(false) },
                books = booksState.books,
                actions = booksState.bookBulkMenuActions(),
                selectedBooks = booksState.selectedBooks,
                onBookSelect = booksState::onBookSelect
            )
        } else SeriesToolBar(series, seriesMenuActions, onBackButtonClick)

        val scrollState = rememberScrollState()
        Box {
            Column(
                modifier = contentPadding.verticalScroll(scrollState),
            ) {

                if (series != null) Series(series, onFilterClick)

                TabRow(
                    currentTab = currentTab,
                    onTabChange = onTabChange,
                    showCollectionsTab = collectionsState.collections.isNotEmpty() && !booksState.booksSelectionMode
                )

                when (currentTab) {
                    SeriesTab.BOOKS -> SeriesBooksContent(
                        booksState = booksState,
                        onBookClick = onBookClick,
                        onBookReadClick = onBookReadClick,
                        scrollState = scrollState
                    )

                    SeriesTab.COLLECTIONS -> SeriesCollectionsContent(
                        state = collectionsState,
                        onCollectionClick = onCollectionClick,
                        onSeriesClick = onSeriesClick
                    )
                }

            }
            VerticalScrollbar(scrollState, Modifier.align(Alignment.CenterEnd))
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
    onFilterClick: (SeriesScreenFilter) -> Unit,
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
    onFilterClick: (SeriesScreenFilter) -> Unit,
    modifier: Modifier
) {
    val contentSize = when (LocalWindowWidth.current) {
        COMPACT, MEDIUM -> Modifier.padding(10.dp, 0.dp)
        EXPANDED -> Modifier.padding(10.dp, 0.dp)
        FULL -> Modifier.padding(30.dp, 0.dp).widthIn(max = 1200.dp)
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
                onClick = { onFilterClick(SeriesScreenFilter(publicationStatus = listOf(series.metadata.status))) },
                label = { Text(series.metadata.status.name) },
                border = null,
                colors =
                when (series.metadata.status) {
                    ENDED -> SuggestionChipDefaults.suggestionChipColors(
                        containerColor = MaterialTheme.colorScheme.secondary,
                        labelColor = MaterialTheme.colorScheme.onSecondary
                    )

                    ONGOING -> SuggestionChipDefaults.suggestionChipColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant,
                        labelColor = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    ABANDONED -> SuggestionChipDefaults.suggestionChipColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer,
                        labelColor = MaterialTheme.colorScheme.onErrorContainer
                    )

                    HIATUS -> SuggestionChipDefaults.suggestionChipColors(
                        containerColor = MaterialTheme.colorScheme.tertiaryContainer,
                        labelColor = MaterialTheme.colorScheme.onTertiaryContainer
                    )
                },
            )

            series.metadata.ageRating?.let { age ->
                SuggestionChip(
                    onClick = { onFilterClick(SeriesScreenFilter(ageRating = listOf(age))) },
                    label = { Text("$age+") }
                )
            }

            if (series.metadata.language.isNotBlank())
                SuggestionChip(
                    onClick = { onFilterClick(SeriesScreenFilter(language = listOf(series.metadata.language))) },
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
            text = series.metadata.summary,
            style = MaterialTheme.typography.bodyMedium,
        )
    }
}

@Composable
fun SeriesInfoLower(
    series: KomgaSeries,
    onFilterClick: (SeriesScreenFilter) -> Unit,
) {
    Column(
        modifier = Modifier.padding(0.dp, 30.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        if (series.metadata.publisher.isNotBlank()) {
            DescriptionChips(
                label = "PUBLISHER",
                chipValue = stringEntry(series.metadata.publisher),
                onClick = { onFilterClick(SeriesScreenFilter(publisher = listOf(it))) }
            )
        }
        DescriptionChips(
            label = "GENRES",
            chipValues = series.metadata.genres.map { stringEntry(it) },
            onChipClick = { onFilterClick(SeriesScreenFilter(genres = listOf(it))) }
        )
        DescriptionChips(
            label = "TAGS",
            chipValues = series.metadata.tags.map { stringEntry(it) },
            secondaryValues = series.booksMetadata.tags.map { stringEntry(it) },
            onChipClick = { onFilterClick(SeriesScreenFilter(tags = listOf(it))) }
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
                    onChipClick = { onFilterClick(SeriesScreenFilter(authors = listOf(it))) }
                )
            }

        series.booksMetadata.authors
            .filter { it.role == "penciller" }
            .groupBy { it.role }
            .forEach { (_, author) ->
                DescriptionChips(
                    label = "PENCILLERS",
                    chipValues = author.map { LabeledEntry(it, it.name) },
                    onChipClick = { onFilterClick(SeriesScreenFilter(authors = listOf(it))) }
                )
            }
    }
}

@Composable
private fun TabRow(
    currentTab: SeriesTab,
    onTabChange: (SeriesTab) -> Unit,
    showCollectionsTab: Boolean,
) {
    val chipColors = AppFilterChipDefaults.filterChipColors()
    Column {
        AnimatedVisibility(showCollectionsTab) {
            Row(horizontalArrangement = Arrangement.spacedBy(5.dp)) {
                Box(Modifier.clickable { onTabChange(SeriesTab.BOOKS) }) {

                }
                FilterChip(
                    onClick = { onTabChange(SeriesTab.BOOKS) },
                    selected = currentTab == SeriesTab.BOOKS,
                    label = { Text("Books") },
                    colors = chipColors,
                    border = null,
                )
                FilterChip(
                    onClick = { onTabChange(SeriesTab.COLLECTIONS) },
                    selected = currentTab == SeriesTab.COLLECTIONS,
                    label = { Text("Collections") },
                    colors = chipColors,
                    border = null,
                )
            }
        }
        HorizontalDivider()
    }
}