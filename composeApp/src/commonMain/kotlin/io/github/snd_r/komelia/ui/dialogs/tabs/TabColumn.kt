package io.github.snd_r.komelia.ui.dialogs.tabs

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.SubcomposeLayout
import androidx.compose.ui.platform.debugInspectorInfo
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import io.github.snd_r.komelia.ui.dialogs.tabs.TabRowDefaults.Indicator
import io.github.snd_r.komelia.ui.dialogs.tabs.TabRowDefaults.tabIndicatorOffset


@Composable
fun TabColumn(
    selectedTabIndex: Int,
    modifier: Modifier = Modifier,
    indicator: @Composable (tabPositions: List<TabPosition>) -> Unit = @Composable { tabPositions ->
        if (selectedTabIndex < tabPositions.size) {
            Indicator(Modifier.tabIndicatorOffset(tabPositions[selectedTabIndex]))
        }
    },
    divider: @Composable () -> Unit = @Composable { HorizontalDivider() },
    tabs: @Composable () -> Unit
) {
    SubcomposeLayout {
        val tabMeasurables = subcompose(TabSlots.Tabs, tabs)
        val tabPlaceables = tabMeasurables.map { it.measure(Constraints()) }

        val containerSize = tabPlaceables.fold(initial = IntSize.Zero) { max, placeable ->
            IntSize(
                width = maxOf(placeable.width, max.width),
                height = placeable.height + max.height
            )
        }

        val resizedTabPlaceables = subcompose(null, tabs)
            .map { it.measure(Constraints(minWidth = containerSize.width)) }

        val tabPositions = resizedTabPlaceables.mapIndexed { index, placeable ->
            TabPosition(placeable.height.toDp() * index, placeable.height.toDp())
        }


        layout(containerSize.width, containerSize.height) {
            resizedTabPlaceables.forEachIndexed { index, placeable ->
                placeable.placeRelative(0, index * placeable.height)
            }

            subcompose(TabSlots.Indicator) { indicator(tabPositions) }.forEach {
                it.measure(Constraints.fixed(containerSize.width, containerSize.height))
                    .placeRelative(0, 0)
            }
        }
    }
}

private enum class TabSlots {
    Tabs,
    Divider,
    Indicator
}

@Immutable
data class TabPosition(val top: Dp, val height: Dp)

object TabRowDefaults {
    fun Modifier.tabIndicatorOffset(currentTabPosition: TabPosition): Modifier = composed(
        inspectorInfo = debugInspectorInfo {
            name = "tabIndicatorOffset"
            value = currentTabPosition
        }
    ) {
        val currentTabHeight by animateDpAsState(
            targetValue = currentTabPosition.height,
            animationSpec = tween(durationMillis = 250, easing = FastOutSlowInEasing)
        )
        val indicatorOffset by animateDpAsState(
            targetValue = currentTabPosition.top,
            animationSpec = tween(durationMillis = 250, easing = FastOutSlowInEasing)
        )
        fillMaxHeight()
            .wrapContentSize(Alignment.TopStart)
            .offset(y = indicatorOffset)
            .height(currentTabHeight)
    }

    @Composable
    fun Indicator(
        modifier: Modifier = Modifier,
        width: Dp = 3.dp,
        color: Color = MaterialTheme.colorScheme.secondary
    ) {
        Box(
            modifier
                .fillMaxHeight()
                .width(width)
                .background(color = color)
        )
    }
}

