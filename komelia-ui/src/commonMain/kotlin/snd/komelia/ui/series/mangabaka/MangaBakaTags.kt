package snd.komelia.ui.series.mangabaka

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.LocalMinimumInteractiveComponentSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SelectableChipColors
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.BlurredEdgeTreatment
import androidx.compose.ui.draw.blur
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.layout.Placeable
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import snd.komelia.ui.LocalWindowWidth
import snd.komelia.ui.common.components.AppFilterChipDefaults
import snd.komelia.ui.common.components.dashedBorder
import snd.komelia.ui.icons.AppIcons
import snd.komelia.ui.icons.mangabaka.BadgeArrow1
import snd.komelia.ui.icons.mangabaka.BadgeArrow2
import snd.komelia.ui.icons.mangabaka.BadgeArrow3
import snd.komelia.ui.icons.mangabaka.BadgeArrow4
import snd.komelia.ui.icons.outlined.Visibility
import snd.komelia.ui.icons.outlined.VisibilityOff
import snd.komelia.ui.icons.outlined.WandStars
import snd.komelia.ui.platform.WindowSizeClass.COMPACT
import snd.komelia.ui.platform.WindowSizeClass.EXPANDED
import snd.komelia.ui.platform.WindowSizeClass.FULL
import snd.komelia.ui.platform.WindowSizeClass.MEDIUM
import snd.komelia.ui.series.mangabaka.state.MangaBakaTagsState
import snd.komelia.ui.series.mangabaka.state.MangaBakaTagsState.BakaTag
import snd.komelia.ui.series.mangabaka.state.MangaBakaTagsState.BakaTagNode
import snd.komf.api.mangabaka.MangaBakaTagWeight.CORE
import snd.komf.api.mangabaka.MangaBakaTagWeight.DEFINING
import snd.komf.api.mangabaka.MangaBakaTagWeight.INCIDENTAL
import snd.komf.api.mangabaka.MangaBakaTagWeight.RECURRENT
import snd.komf.api.mangabaka.MangaBakaTagWeight.UNWEIGHTED
import kotlin.math.roundToInt

@Composable
fun MangaBakaTags(state: MangaBakaTagsState) {
    val windowWidth = LocalWindowWidth.current
    val overflowHeight = remember(windowWidth) {
        when (windowWidth) {
            COMPACT, MEDIUM -> 600.dp
            EXPANDED, FULL -> 500.dp
        }
    }

    ExpandableBox(
        overflowHeight = overflowHeight
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(5.dp),
        ) {
            MangaBakaGridLayout {
                for (root in state.displayTags) {
                    TagGroup(
                        modifier = Modifier,
                        title = root.tag.name.uppercase(),
                        nodes = root.children,
                        showSpoilers = state.showSpoiler,
                        showImplied = state.showImplied,
                        topLevel = true
                    )
                }
            }
        }
    }
}

@Composable
fun MangaBakaTagFilters(
    tagsState: MangaBakaTagsState,
) {
    CompositionLocalProvider(LocalMinimumInteractiveComponentSize provides Dp.Unspecified) {
        val windowWidth = LocalWindowWidth.current
        BoxWithConstraints {
            val maxWidth = maxWidth
            val showLabels = maxWidth > 800.dp
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(5.dp),
                verticalArrangement = Arrangement.spacedBy(5.dp)
            ) {
                when (windowWidth) {
                    COMPACT, MEDIUM -> {
                        TagWeightFilters(tagsState, showLabels)
                        TagOtherFilters(tagsState)
                    }

                    EXPANDED, FULL -> {
                        TagOtherFilters(tagsState)
                        TagWeightFilters(tagsState, showLabels)
                    }
                }

            }
        }
    }
}

@Composable
private fun TagWeightFilters(
    tagsState: MangaBakaTagsState,
    showLabels: Boolean,
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(5.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {

        FilterChip(
            selected = tagsState.weight == UNWEIGHTED,
            onClick = { tagsState.onFilterChange(UNWEIGHTED) },
            label = { Text("All", style = MaterialTheme.typography.labelLarge) },
            colors = AppFilterChipDefaults.filterChipColors(),
            border = null,
        )

        val incidentalSelected = tagsState.weight == INCIDENTAL
        FilterChip(
            selected = incidentalSelected,
            onClick = { tagsState.onFilterChange(INCIDENTAL) },
            label = {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = AppIcons.BadgeArrow1,
                        contentDescription = null,
                        tint = if (incidentalSelected) MaterialTheme.colorScheme.onPrimary else LocalContentColor.current
                    )
                    if (showLabels) Text("Incidental", style = MaterialTheme.typography.labelLarge)
                }
            },
            colors = AppFilterChipDefaults.filterChipColors(),
            border = null,
        )

        val recurrentSelected = tagsState.weight == RECURRENT
        FilterChip(
            selected = recurrentSelected,
            onClick = { tagsState.onFilterChange(RECURRENT) },
            label = {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = AppIcons.BadgeArrow2,
                        contentDescription = null,
                        tint = if (recurrentSelected) MaterialTheme.colorScheme.onPrimary else LocalContentColor.current
                    )
                    if (showLabels) Text("Recurrent", style = MaterialTheme.typography.labelLarge)
                }
            },
            colors = AppFilterChipDefaults.filterChipColors(),
            border = null,
        )

        val definingSelected = tagsState.weight == DEFINING
        FilterChip(
            selected = definingSelected,
            onClick = { tagsState.onFilterChange(DEFINING) },
            label = {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = AppIcons.BadgeArrow3,
                        contentDescription = null,
                        tint = if (definingSelected) MaterialTheme.colorScheme.onPrimary else LocalContentColor.current
                    )
                    if (showLabels) Text("Defining", style = MaterialTheme.typography.labelLarge)
                }
            },
            colors = AppFilterChipDefaults.filterChipColors(),
            border = null,
        )

        val coreSelected = tagsState.weight == CORE
        FilterChip(
            selected = coreSelected,
            onClick = { tagsState.onFilterChange(CORE) },
            label = {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = AppIcons.BadgeArrow4,
                        contentDescription = null,
                        tint = if (coreSelected) MaterialTheme.colorScheme.onPrimary else LocalContentColor.current
                    )
                    if (showLabels) Text("Core", style = MaterialTheme.typography.labelLarge)
                }
            },
            colors = AppFilterChipDefaults.filterChipColors(),
            border = null,
        )
    }

}

@Composable
private fun TagOtherFilters(
    tagsState: MangaBakaTagsState,
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(5.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (tagsState.impliedCount > 0) {
            FilterChip(
                modifier = Modifier.dashedBorder(
                    width = 1.dp,
                    color = MaterialTheme.colorScheme.secondary,
                    shape = RoundedCornerShape(8.dp),
                    on = 5.dp,
                    off = 5.dp
                ),
                selected = tagsState.showImplied,
                onClick = { tagsState.onImpliedShowChange(!tagsState.showImplied) },
                leadingIcon = {
                    Icon(
                        imageVector = AppIcons.Outlined.WandStars,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)

                    )
                },
                label = {
                    Text(
                        "${tagsState.impliedCount} implied",
                        style = MaterialTheme.typography.labelLarge,
                    )
                },
                colors = FilterChipDefaults.filterChipColors(
                    containerColor = MaterialTheme.colorScheme.secondary.copy(alpha = .1f),
                    selectedContainerColor = MaterialTheme.colorScheme.secondary.copy(alpha = .7f),
                    selectedLabelColor = MaterialTheme.colorScheme.onSecondary
                ),
                border = null,
                elevation = null
            )
        }
        if (tagsState.spoilerCount > 0) {
            FilterChip(
                selected = tagsState.showSpoiler,
                onClick = { tagsState.onSpoilerShowChange(!tagsState.showSpoiler) },
                leadingIcon = {
                    Icon(
                        imageVector = if (tagsState.showSpoiler) AppIcons.Outlined.Visibility
                        else AppIcons.Outlined.VisibilityOff,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                },
                label = {
                    Text(
                        "${tagsState.spoilerCount} spoilers",
                        style = MaterialTheme.typography.labelLarge
                    )
                },
                colors = FilterChipDefaults.filterChipColors(
                    containerColor = MaterialTheme.colorScheme.tertiary.copy(alpha = .3f),
                    selectedContainerColor = MaterialTheme.colorScheme.tertiary.copy(alpha = .7f),
                    selectedLabelColor = MaterialTheme.colorScheme.onTertiary
                ),
                border = FilterChipDefaults.filterChipBorder(
                    enabled = true,
                    selected = tagsState.showSpoiler,
                    borderColor = MaterialTheme.colorScheme.tertiary
                ),
                elevation = null
            )

        }
    }
}

@Composable
fun TagChip(
    node: BakaTagNode,
    showSpoilers: Boolean,
    showImplied: Boolean,
) {
    if (!node.tag.isExplicit && !showImplied) return
    val interactionSource = remember { MutableInteractionSource() }
    val isHovered = interactionSource.collectIsHoveredAsState().value
    val isFocused = interactionSource.collectIsFocusedAsState().value

    val tag = node.tag
    val (colors, borderColors) = tagChipColors(tag)
    val blurModifier = remember(tag, showSpoilers, isHovered, isFocused) {
        if (tag.isSpoiler && !showSpoilers && !isHovered && !isFocused) Modifier.blur(
            radius = 8.dp,
            edgeTreatment = BlurredEdgeTreatment(RoundedCornerShape(5.dp))
        ) else Modifier
    }
    val impliedBorderColor = MaterialTheme.colorScheme.secondary
    val impliedModifier = remember(tag) {
        if (!tag.isExplicit) {
            Modifier.dashedBorder(
                width = 1.dp,
                color = impliedBorderColor,
                shape = RoundedCornerShape(8.dp),
                on = 5.dp,
                off = 5.dp
            )

        } else Modifier
    }

    FilterChip(
        modifier = impliedModifier,
        selected = false,
        onClick = { },
        trailingIcon = {
            val icon = remember(tag) {
                when (tag.weight) {
                    CORE -> AppIcons.BadgeArrow4
                    DEFINING -> AppIcons.BadgeArrow3
                    RECURRENT -> AppIcons.BadgeArrow2
                    INCIDENTAL -> AppIcons.BadgeArrow1
                    UNWEIGHTED -> null
                }
            }
            icon?.let { Icon(icon, null, modifier = Modifier.padding(end = 10.dp)) }
        },
        label = {
            Text(
                text = buildAnnotatedString {
                    if (node.reducedPrefixes.isNotEmpty()) {
                        withStyle(
                            MaterialTheme.typography.labelMedium.toSpanStyle()
                                .copy(color = colors.labelColor.copy(alpha = .5f))
                        ) {
                            append(node.reducedPrefixes.joinToString(" > ") { it.name })
                            append(" > ")
                        }
                    }
                    withStyle(MaterialTheme.typography.labelMedium.toSpanStyle()) {
                        append(tag.name)
                    }
                },
                style = MaterialTheme.typography.labelMedium,
                modifier = blurModifier
            )
        },
        colors = colors,
        border = borderColors,
        interactionSource = interactionSource,
        elevation = null
    )
}

@Composable
fun TagGroup(
    modifier: Modifier,
    title: String,
    nodes: List<BakaTagNode>,
    showSpoilers: Boolean,
    showImplied: Boolean,
    topLevel: Boolean
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(5.dp),
        modifier = modifier
    ) {
        CompositionLocalProvider(LocalMinimumInteractiveComponentSize provides Dp.Unspecified) {
            Text(
                text = title,
                color = if (topLevel) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.primary.copy(
                    alpha = .5f
                ),
                style = if (topLevel) MaterialTheme.typography.labelLargeEmphasized else MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold
            )
            ColorSpacerContent(
                color = if (topLevel) MaterialTheme.colorScheme.secondary
                else MaterialTheme.colorScheme.surfaceVariant
            ) {
                FlowRow(
                    verticalArrangement = Arrangement.spacedBy(5.dp),
                    horizontalArrangement = Arrangement.spacedBy(5.dp),
                ) {
                    for (node in nodes) {
                        if (node.children.isNotEmpty()) {
                            TagGroup(
                                modifier = Modifier.fillMaxWidth(),
                                title = buildString {
                                    node.reducedPrefixes.forEach {
                                        append(it.name)
                                        append(" > ")
                                    }
                                    append(node.tag.name)
                                },
                                nodes = node.children,
                                showSpoilers = showSpoilers,
                                showImplied = showImplied,
                                topLevel = false
                            )
                        } else {
                            TagChip(
                                node = node,
                                showSpoilers = showSpoilers,
                                showImplied = showImplied,
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ColorSpacerContent(
    color: Color = MaterialTheme.colorScheme.secondary,
    content: @Composable () -> Unit,
) {
    Layout(content = {
        content()
        Box(
            Modifier
                .fillMaxHeight()
                .width(3.dp)
                .background(color)
        )
    }) { measurables, constraints ->
        val content = measurables[0].measure(constraints)
        val colorSpacer = measurables[1].measure(constraints.copy(maxHeight = content.height))
        val spacing = 10.dp.toPx().roundToInt()

        val width = (colorSpacer.width + spacing + content.width).coerceAtMost(constraints.maxWidth)
        layout(width, content.height) {
            colorSpacer.placeRelative(0, 0)
            content.placeRelative(colorSpacer.width + spacing, 0)
        }
    }
}


@Composable
private fun tagChipColors(tag: BakaTag): Pair<SelectableChipColors, BorderStroke?> {
    val colors = if (tag.isSpoiler) {
        FilterChipDefaults.filterChipColors(
            containerColor = MaterialTheme.colorScheme.tertiary.copy(alpha = .3f),
        ) to FilterChipDefaults.filterChipBorder(
            enabled = true,
            selected = false,
            borderColor = MaterialTheme.colorScheme.tertiary,
        )
    } else if (!tag.isExplicit) {
        FilterChipDefaults.filterChipColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = .3f),
        ) to null

    } else {
        when (tag.weight) {
            CORE -> FilterChipDefaults.filterChipColors(
                containerColor = MaterialTheme.colorScheme.secondary,
                labelColor = MaterialTheme.colorScheme.onSecondary,
                iconColor = MaterialTheme.colorScheme.onSecondary,
            ) to FilterChipDefaults.filterChipBorder(
                enabled = true,
                selected = false,
                borderColor = MaterialTheme.colorScheme.secondary,
            )

            DEFINING -> FilterChipDefaults.filterChipColors(
                containerColor = MaterialTheme.colorScheme.secondary.copy(alpha = .6f),
                labelColor = MaterialTheme.colorScheme.onSecondary,
                iconColor = MaterialTheme.colorScheme.onSecondary,
            ) to FilterChipDefaults.filterChipBorder(
                enabled = true,
                selected = false,
                borderColor = MaterialTheme.colorScheme.secondary,
            )

            RECURRENT -> FilterChipDefaults.filterChipColors(
                containerColor = MaterialTheme.colorScheme.secondary.copy(alpha = .2f),
                iconColor = MaterialTheme.colorScheme.onSecondary,
            ) to FilterChipDefaults.filterChipBorder(
                enabled = true,
                selected = false,
                borderColor = MaterialTheme.colorScheme.secondary,
            )

            INCIDENTAL -> FilterChipDefaults.filterChipColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = .7f),
                iconColor = MaterialTheme.colorScheme.onSurfaceVariant,
            ) to FilterChipDefaults.filterChipBorder(
                enabled = true,
                selected = false,
                borderColor = Color.Unspecified,
            )

            UNWEIGHTED -> FilterChipDefaults.filterChipColors(
                containerColor = MaterialTheme.colorScheme.surface,
                iconColor = MaterialTheme.colorScheme.onSurface,
            ) to FilterChipDefaults.filterChipBorder(
                enabled = true,
                selected = false,
                borderColor = MaterialTheme.colorScheme.surfaceVariant,
            )
        }

    }
    return colors
}

@Composable
private fun MangaBakaGridLayout(
    rowGap: Dp = 15.dp,
    columnGap: Dp = 30.dp,
    content: @Composable () -> Unit,
) {
    Layout(content = content) { measurables, constraints ->
        val rowGapPx = rowGap.toPx().roundToInt()
        val columnGapPx = columnGap.toPx().roundToInt()
        val columnsCount = when (constraints.maxWidth.toDp()) {
            in 0.dp..600.dp -> 1
            in 600.dp..900.dp -> 2
            else -> 3
        }
        val columnWidth = constraints.maxWidth / columnsCount
        val totalWidth = when (measurables.size) {
            1 -> columnWidth
            2 -> columnWidth * 2
            else -> constraints.maxWidth
        }


        val measurableRows = measurables.chunked(columnsCount)
        val placeables = mutableListOf<List<Placeable>>()
        val columnHeights = IntArray(columnsCount)
        val columnConstraint = constraints.copy(
            minWidth = constraints.minWidth,
            maxWidth = (columnWidth - columnGapPx).coerceAtLeast(0)
        )
        for (row in measurableRows) {
            val rowPlaceables = mutableListOf<Placeable>()
            for ((columnIndex, measurable) in row.withIndex()) {
                val placeable = measurable.measure(columnConstraint)
                rowPlaceables.add(placeable)
                columnHeights[columnIndex] += placeable.height + rowGapPx
            }

            placeables.add(rowPlaceables)
        }

        val columnOffsets = (0..columnsCount).map { i -> (columnWidth * i) + if (i != 0) columnGapPx else 0 }
        val columnHeightOffsets = IntArray(columnsCount)
        layout(totalWidth, (columnHeights.max() - rowGapPx).coerceAtLeast(0)) {
            for (row in placeables) {
                for ((i, placeable) in row.withIndex()) {
                    placeable.place(columnOffsets[i], columnHeightOffsets[i])
                    columnHeightOffsets[i] = columnHeightOffsets[i] + placeable.height + rowGapPx
                }
            }
        }
    }
}
