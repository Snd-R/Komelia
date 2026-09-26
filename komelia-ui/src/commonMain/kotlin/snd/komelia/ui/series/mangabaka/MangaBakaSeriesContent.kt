package snd.komelia.ui.series.mangabaka

import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.FiniteAnimationSpec
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SecondaryTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.input.pointer.PointerIcon
import androidx.compose.ui.input.pointer.pointerHoverIcon
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import snd.komelia.komga.api.model.KomeliaSeries
import snd.komelia.ui.LocalWindowWidth
import snd.komelia.ui.common.images.SeriesThumbnail
import snd.komelia.ui.common.menus.SeriesMenuActions
import snd.komelia.ui.icons.AppIcons
import snd.komelia.ui.icons.mangabaka.flags.FlagJp
import snd.komelia.ui.icons.mangabaka.flags.FlagUS
import snd.komelia.ui.icons.outlined.ChevronForward
import snd.komelia.ui.icons.outlined.KeyboardDoubleArrowDown
import snd.komelia.ui.icons.outlined.KeyboardDoubleArrowUp
import snd.komelia.ui.icons.outlined.NewsStand
import snd.komelia.ui.platform.WindowSizeClass.COMPACT
import snd.komelia.ui.platform.WindowSizeClass.EXPANDED
import snd.komelia.ui.platform.WindowSizeClass.FULL
import snd.komelia.ui.platform.WindowSizeClass.MEDIUM
import snd.komelia.ui.platform.cursorForHand
import snd.komelia.ui.series.mangabaka.state.MangaBakaMetadataState
import snd.komelia.ui.series.mangabaka.state.MangaBakaMetadataState.MangaBakaContentTab.DESCRIPTION
import snd.komelia.ui.series.mangabaka.state.MangaBakaMetadataState.MangaBakaContentTab.LINKS
import snd.komelia.ui.series.mangabaka.state.MangaBakaMetadataState.MangaBakaContentTab.META
import snd.komelia.ui.series.mangabaka.state.MangaBakaMetadataState.MangaBakaContentTab.TAGS
import snd.komelia.ui.series.mangabaka.state.MangaBakaTitlesState
import snd.komelia.ui.series.view.KomfSeriesActionsButton
import snd.komelia.ui.series.view.SeriesActionMenuButton
import snd.komelia.ui.series.view.SeriesEditDialogButton
import snd.komga.client.library.KomgaLibrary
import kotlin.math.roundToInt

@Composable
fun MangaBakaSeriesContent(
    series: KomeliaSeries,
    actions: SeriesMenuActions,
    library: KomgaLibrary,
    mangaBakaState: MangaBakaMetadataState,
    onLibraryClick: (KomgaLibrary) -> Unit,
) {
    val width = LocalWindowWidth.current
    when (width) {
        COMPACT, MEDIUM -> CompactSizeContent(
            series = series,
            actions = actions,
            library = library,
            mangaBakaState = mangaBakaState,
            onLibraryClick = onLibraryClick
        )

        EXPANDED,
        FULL -> Column {
            AltTitles(mangaBakaState.titleState)
            Row(Modifier.padding(bottom = 10.dp)) {
                ThumbnailAndMeta(
                    modifier = Modifier.widthIn(max = 440.dp).padding(end = 10.dp),
                    series = series,
                    mangaBakaState = mangaBakaState
                )
                DescriptionAndTags(mangaBakaState = mangaBakaState)
            }
        }
    }
}

@Composable
private fun CompactSizeContent(
    series: KomeliaSeries,
    actions: SeriesMenuActions,
    library: KomgaLibrary,
    mangaBakaState: MangaBakaMetadataState,
    onLibraryClick: (KomgaLibrary) -> Unit,
) {
    Column {
        MangaBakaSeriesCompactToolbar(
            series = series,
            actions = actions,
            library = library,
            mangaBakaState = mangaBakaState,
            onLibraryClick = onLibraryClick
        )
        SeriesThumbnail(
            seriesId = series.id,
            modifier = Modifier
                .animateContentSize(animationSpec = spring(stiffness = Spring.StiffnessHigh))
                .heightIn(min = 100.dp, max = 400.dp)
                .widthIn(min = 300.dp, max = 440.dp)
                .align(Alignment.CenterHorizontally),
            contentScale = ContentScale.Fit
        )
        val currentTab = mangaBakaState.compactContentTab.collectAsState().value
        SecondaryTabRow(
            currentTab.ordinal,
        ) {
            Tab(
                modifier = Modifier.heightIn(min = 40.dp).cursorForHand(),
                selected = currentTab == META,
                onClick = { mangaBakaState.onCompactTabSelect(META) },
                content = { Text("Meta") }
            )
            Tab(
                modifier = Modifier.heightIn(min = 40.dp).cursorForHand(),
                selected = currentTab == DESCRIPTION,
                onClick = { mangaBakaState.onCompactTabSelect(DESCRIPTION) },
                content = { Text("Description") }
            )
            Tab(
                modifier = Modifier.heightIn(min = 40.dp).cursorForHand(),
                selected = currentTab == LINKS,
                onClick = { mangaBakaState.onCompactTabSelect(LINKS) },
                content = { Text("Links") }
            )
            Tab(
                modifier = Modifier.heightIn(min = 40.dp).cursorForHand(),
                selected = currentTab == TAGS,
                onClick = { mangaBakaState.onCompactTabSelect(TAGS) },
                content = { Text("Tags") }
            )
        }
        Spacer(Modifier.height(8.dp))

        when (currentTab) {
            META -> MangaBakaMeta(mangaBakaState)
            DESCRIPTION -> MangaBakaDescription(mangaBakaState.metadata.description ?: "")
            LINKS -> MangaBakaLinks(mangaBakaState.linksState)
            TAGS -> {
                Column(
                    verticalArrangement = Arrangement.spacedBy(5.dp),
                ) {
                    MangaBakaTagFilters(mangaBakaState.tagsState)
                    MangaBakaTags(mangaBakaState.tagsState)
                }
            }
        }
        Spacer(Modifier.height(32.dp))
    }
}

@Composable
fun MangaBakaSeriesCompactToolbar(
    series: KomeliaSeries,
    actions: SeriesMenuActions,
    library: KomgaLibrary,
    mangaBakaState: MangaBakaMetadataState,
    onLibraryClick: (KomgaLibrary) -> Unit,
) {
    Column {
        FlowRow(
            itemVerticalAlignment = Alignment.CenterVertically,
        ) {
            Row(
                modifier = Modifier
                    .clickable(onClick = { onLibraryClick(library) })
                    .pointerHoverIcon(PointerIcon.Hand),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(5.dp)
            ) {
                val tintColor = MaterialTheme.colorScheme.primary
                Icon(
                    imageVector = AppIcons.Outlined.NewsStand,
                    contentDescription = null,
                    tint = tintColor
                )
                Text(
                    library.name,
                    style = MaterialTheme.typography.labelMedium,
                    color = tintColor
                )
                Icon(
                    imageVector = AppIcons.Outlined.ChevronForward,
                    contentDescription = null,
                    tint = tintColor,
                    modifier = Modifier.size(16.dp),
                )
            }
            Spacer(Modifier.widthIn(5.dp))

            SelectionContainer {
                Text(
                    text = mangaBakaState.titleState.mainTitle.title,
                    overflow = TextOverflow.Ellipsis,
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.weight(1f, false)
                )
            }
        }
        AltTitles(mangaBakaState.titleState)
        HorizontalDivider()
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(5.dp)
        ) {
            SeriesEditDialogButton(series)
            KomfSeriesActionsButton(series)
            SeriesActionMenuButton(series, actions)
        }
    }
}

@Composable
private fun ThumbnailAndMeta(
    modifier: Modifier = Modifier,
    series: KomeliaSeries,
    mangaBakaState: MangaBakaMetadataState,
) {
    val width = LocalWindowWidth.current
    val animation: FiniteAnimationSpec<IntSize> = remember(series) {
        when (width) {
            COMPACT, MEDIUM -> spring(stiffness = Spring.StiffnessHigh)
            else -> spring(stiffness = Spring.StiffnessMediumLow)
        }
    }

    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(5.dp)
    ) {
        SeriesThumbnail(
            seriesId = series.id,
            modifier = Modifier
                .animateContentSize(animationSpec = animation)
                .heightIn(min = 100.dp, max = 400.dp)
                .widthIn(min = 300.dp, max = 440.dp)
                .align(Alignment.CenterHorizontally),
            contentScale = ContentScale.Fit
        )

        MangaBakaMeta(mangaBakaState)
    }
}


@Composable
private fun AltTitles(titleState: MangaBakaTitlesState) {
    val titles = titleState.secondaryTitles
    FlowRow(
        horizontalArrangement = Arrangement.spacedBy(5.dp),
        verticalArrangement = Arrangement.spacedBy(5.dp),
        itemVerticalAlignment = Alignment.CenterVertically,
    ) {
        for ((title, languages) in titles) {
            val flags = languages.mapNotNull {
                when {
                    it.startsWith("en") -> AppIcons.FlagUS
                    it.startsWith("ja") -> AppIcons.FlagJp
                    else -> null
                }
            }.distinct()

            Row(horizontalArrangement = Arrangement.spacedBy(5.dp)) {
                Row {
                    flags.forEach {
                        Image(
                            imageVector = it,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )

                    }
                }
                SelectionContainer {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.primary.copy(alpha = .6f)
                    )
                }
            }
        }
    }
}

@Composable
private fun DescriptionAndTags(
    mangaBakaState: MangaBakaMetadataState,
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(20.dp),
    ) {
        val description = mangaBakaState.metadata.description
        if (!description.isNullOrBlank()) {
            Column(
                verticalArrangement = Arrangement.spacedBy(5.dp)
            ) {
                Text("Description", style = MaterialTheme.typography.headlineSmall)
                HorizontalDivider(color = MaterialTheme.colorScheme.secondary)
                MangaBakaDescription(description)
            }
        }

        Column(
            verticalArrangement = Arrangement.spacedBy(5.dp)
        ) {
            Text("Links", style = MaterialTheme.typography.headlineSmall)
            HorizontalDivider(color = MaterialTheme.colorScheme.secondary)
            MangaBakaLinks(mangaBakaState.linksState)
        }
        Column(
            verticalArrangement = Arrangement.spacedBy(5.dp)
        ) {
            FlowRow(
                itemVerticalAlignment = Alignment.Bottom
            ) {
                Text("Tags", style = MaterialTheme.typography.headlineSmall)
                Spacer(Modifier.weight(1f))
                MangaBakaTagFilters(mangaBakaState.tagsState)

            }
            HorizontalDivider(color = MaterialTheme.colorScheme.secondary)
            MangaBakaTags(mangaBakaState.tagsState)
        }
    }
}


@Composable
private fun ShowMoreButton(onClick: () -> Unit, isExpanded: Boolean) {
    Column(
        modifier = Modifier
            .background(MaterialTheme.colorScheme.surface)
            .padding(top = 10.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        HorizontalDivider()
        Row(
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .clickable(onClick = onClick)
                .pointerHoverIcon(PointerIcon.Hand)
                .padding(vertical = 5.dp)
        ) {
            val icon = remember(isExpanded) {
                if (isExpanded) AppIcons.Outlined.KeyboardDoubleArrowUp
                else AppIcons.Outlined.KeyboardDoubleArrowDown
            }
            Spacer(Modifier.weight(1f))
            Icon(icon, null)
            Text(if (isExpanded) "Hide" else "Show more")
            Icon(icon, null)
            Spacer(Modifier.weight(1f))
        }
    }
}

@Composable
fun ExpandableBox(
    modifier: Modifier = Modifier,
    overflowHeight: Dp,
    content: @Composable () -> Unit,
) {
    var isExpanded by remember { mutableStateOf(false) }
    Layout(
        modifier = modifier.clipToBounds(),
        content = {
            content()
            ShowMoreButton(
                onClick = { isExpanded = !isExpanded },
                isExpanded = isExpanded
            )
        }
    ) { measurables, constraints ->
        val overflowHeightPx = overflowHeight.toPx().roundToInt()
        val mainContent = measurables.first().measure(constraints)
        val expandButton = measurables[1].measure(constraints)

        if (mainContent.height < overflowHeightPx) isExpanded = false
        val maxHeight =
            if (isExpanded) mainContent.height + expandButton.height
            else minOf(overflowHeightPx, mainContent.height)

        val showExpandButton = mainContent.height > maxHeight || isExpanded

        layout(constraints.maxWidth, maxHeight) {
            mainContent.placeRelative(0, 0)
            if (showExpandButton) expandButton.placeRelative(0, maxHeight - expandButton.height)

        }
    }
}