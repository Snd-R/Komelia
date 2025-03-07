package io.github.snd_r.komelia.ui.common.cards

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.hoverable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import io.github.snd_r.komelia.platform.cursorForHand
import io.github.snd_r.komelia.ui.LocalLibraries
import io.github.snd_r.komelia.ui.common.NoPaddingChip
import io.github.snd_r.komelia.ui.common.images.SeriesThumbnail
import io.github.snd_r.komelia.ui.common.menus.SeriesActionsMenu
import io.github.snd_r.komelia.ui.common.menus.SeriesMenuActions
import snd.komga.client.series.KomgaSeries

@Composable
fun SeriesImageCard(
    series: KomgaSeries,
    onSeriesClick: () -> Unit,
    isSelected: Boolean = false,
    onSeriesSelect: (() -> Unit)? = null,
    seriesMenuActions: SeriesMenuActions? = null,
    modifier: Modifier = Modifier,
) {
    val libraries = LocalLibraries.current
    val libraryIsDeleted = remember {
        libraries.value.firstOrNull { it.id == series.libraryId }?.unavailable ?: false
    }
    ItemCard(
        modifier = modifier,
        onClick = onSeriesClick,
        onLongClick = onSeriesSelect,
        image = {
            SeriesCardHoverOverlay(
                series = series,
                onSeriesSelect = onSeriesSelect,
                isSelected = isSelected,
                seriesActions = seriesMenuActions,
            ) {
                SeriesImageOverlay(series = series, libraryIsDeleted = libraryIsDeleted) {
                    SeriesThumbnail(
                        series.id,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                }
            }
        }
    )
}

@Composable
fun SeriesSimpleImageCard(
    series: KomgaSeries,
    onSeriesClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier,
) {
    ItemCard(
        modifier = modifier,
        onClick = onSeriesClick,
        image = {
            SeriesImageOverlay(
                series = series,
                libraryIsDeleted = false,
                showTitle = false,
            ) {
                SeriesThumbnail(
                    series.id,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            }
        }
    )
}

@Composable
private fun SeriesCardHoverOverlay(
    series: KomgaSeries,
    isSelected: Boolean,
    onSeriesSelect: (() -> Unit)?,
    seriesActions: SeriesMenuActions?,
    content: @Composable () -> Unit,
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isHovered = interactionSource.collectIsHoveredAsState()
    var isActionsMenuExpanded by remember { mutableStateOf(false) }
    val showOverlay = derivedStateOf { isHovered.value || isActionsMenuExpanded || isSelected }
    val border = if (showOverlay.value) overlayBorderModifier() else Modifier

    Box(
        modifier = Modifier
            .fillMaxSize()
            .hoverable(interactionSource)
            .then(border),
        contentAlignment = Alignment.Center
    ) {
        content()

        if (showOverlay.value) {
            val backgroundModifier =
                if (isSelected) Modifier.background(MaterialTheme.colorScheme.secondary.copy(alpha = .5f))
                else Modifier
            Column(backgroundModifier.fillMaxSize()) {
                if (onSeriesSelect != null) {
                    SelectionRadioButton(isSelected, onSeriesSelect)
                    Spacer(Modifier.weight(1f))
                }

                if (seriesActions != null) {
                    Row(
                        modifier = Modifier.fillMaxSize(),
                        verticalAlignment = Alignment.Bottom,
                    ) {
                        Spacer(Modifier.weight(1f))

                        Box {
                            IconButton(
                                onClick = { isActionsMenuExpanded = true },
                                colors = IconButtonDefaults.iconButtonColors(containerColor = MaterialTheme.colorScheme.surface)
                            ) {
                                Icon(Icons.Default.MoreVert, contentDescription = null)
                            }

                            SeriesActionsMenu(
                                series = series,
                                actions = seriesActions,
                                expanded = isActionsMenuExpanded,
                                showEditOption = true,
                                onDismissRequest = { isActionsMenuExpanded = false },
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SeriesImageOverlay(
    series: KomgaSeries,
    libraryIsDeleted: Boolean,
    showTitle: Boolean = true,
    content: @Composable () -> Unit
) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.TopStart,
    ) {
        content()
        if (showTitle) {
            CardGradientOverlay()
        }

        if (series.booksUnreadCount > 0) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.TopEnd
            ) {
                Box(
                    modifier = Modifier.size(30.dp).background(MaterialTheme.colorScheme.tertiary),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        "${series.booksUnreadCount}",
                        color = MaterialTheme.colorScheme.onTertiary,
                        style = MaterialTheme.typography.labelLarge
                    )
                }
            }
        }

        Column(
            modifier = Modifier.fillMaxSize().padding(10.dp),
            verticalArrangement = Arrangement.Bottom
        ) {
            if (showTitle) {

                CardOutlinedText(text = series.metadata.title, maxLines = 4)
                if (series.deleted || libraryIsDeleted) {
                    CardOutlinedText(text = "Unavailable", textColor = MaterialTheme.colorScheme.error)
                }
            }
        }
    }
}

@Composable
fun SeriesDetailedListCard(
    series: KomgaSeries,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier
            .cursorForHand()
            .clickable { onClick() }) {
        Row(
            Modifier
                .fillMaxWidth()
                .heightIn(max = 200.dp)
                .padding(10.dp)
        ) {
            SeriesSimpleImageCard(series, onClick)
            SeriesDetails(series)
        }
    }
}

@Composable
private fun SeriesDetails(series: KomgaSeries) {
    Column(Modifier.padding(start = 10.dp)) {
        Row {
            Text(series.metadata.title, fontWeight = FontWeight.Bold)
        }
        LazyRow(
            modifier = Modifier.padding(vertical = 10.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(series.metadata.genres) {
                NoPaddingChip(
                    borderColor = MaterialTheme.colorScheme.surface,
                    color = MaterialTheme.colorScheme.surface
                ) {
                    Text(it, style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                }

            }
        }
        Text(series.metadata.summary, maxLines = 4, style = MaterialTheme.typography.bodyMedium)

    }
}