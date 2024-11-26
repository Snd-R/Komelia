package io.github.snd_r.komelia.platform

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.windowInsetsTopHeight
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.layout.Measurable
import androidx.compose.ui.layout.MeasurePolicy
import androidx.compose.ui.layout.MeasureResult
import androidx.compose.ui.layout.MeasureScope
import androidx.compose.ui.layout.Placeable
import androidx.compose.ui.node.ModifierNodeElement
import androidx.compose.ui.node.ParentDataModifierNode
import androidx.compose.ui.platform.InspectableValue
import androidx.compose.ui.platform.InspectorInfo
import androidx.compose.ui.platform.NoInspectorInfo
import androidx.compose.ui.platform.debugInspectorInfo
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.offset
import kotlin.math.max

@Composable
expect fun PlatformTitleBar(
    modifier: Modifier = Modifier,
    applyInsets: Boolean = true,
    fallbackToNonPlatformLayout: Boolean = true,
    content: @Composable TitleBarScope.() -> Unit = {},
)

@Composable
fun SimpleTitleBarLayout(
    modifier: Modifier = Modifier,
    applyInsets: Boolean,
    content: @Composable TitleBarScope.() -> Unit,
) {
    Column {
        if (applyInsets) Spacer(Modifier.windowInsetsTopHeight(WindowInsets.systemBars))
        TitleBarLayout(
            modifier = modifier,
            applyTitleBar = { _ -> PaddingValues(0.dp) },
            applyContentWidth = { _, _, _ -> },
            content = content
        )

    }

}

@Composable
fun TitleBarLayout(
    modifier: Modifier = Modifier,
    applyTitleBar: (height: Dp) -> PaddingValues,
    applyContentWidth: (start: Pair<Dp, Dp>?, center: Pair<Dp, Dp>?, end: Pair<Dp, Dp>?) -> Unit,
    content: @Composable TitleBarScope.() -> Unit,
) {
    Layout(
        content = { TitleBarScopeImpl().content() },
        modifier = modifier.fillMaxWidth(),
        measurePolicy = remember(applyTitleBar) {
            TitleBarMeasurePolicy(
                applyTitleBar,
                applyContentWidth
            )
        }
    )
}

interface TitleBarScope {
    @Stable
    fun Modifier.align(alignment: Alignment.Horizontal): Modifier
}

internal class TitleBarMeasurePolicy(
    private val applyTitleBar: (height: Dp) -> PaddingValues,
    private val applyContentWidth: (start: Pair<Dp, Dp>?, center: Pair<Dp, Dp>?, end: Pair<Dp, Dp>?) -> Unit,
) : MeasurePolicy {

    override fun MeasureScope.measure(
        measurables: List<Measurable>,
        constraints: Constraints
    ): MeasureResult {
        if (measurables.isEmpty()) {
            applyTitleBar(constraints.minHeight.toDp())
            return layout(width = constraints.minWidth, height = constraints.minHeight) {}
        }

        var occupiedSpaceHorizontally = 0

        var maxSpaceVertically = constraints.minHeight
        val contentConstraints = constraints.copy(minWidth = 0, minHeight = 0)
        val measuredPlaceable = mutableListOf<Pair<Measurable, Placeable>>()

        for (it in measurables) {
            val placeable =
                it.measure(contentConstraints.offset(horizontal = -occupiedSpaceHorizontally))
            if (constraints.maxWidth < occupiedSpaceHorizontally + placeable.width) {
                break
            }
            occupiedSpaceHorizontally += placeable.width
            maxSpaceVertically = max(maxSpaceVertically, placeable.height)
            measuredPlaceable += it to placeable
        }

        val boxHeight = maxSpaceVertically

        val contentPadding = applyTitleBar(boxHeight.toDp())

        val leftInset = contentPadding.calculateLeftPadding(layoutDirection).roundToPx()
        val rightInset = contentPadding.calculateRightPadding(layoutDirection).roundToPx()

        occupiedSpaceHorizontally += leftInset
        occupiedSpaceHorizontally += rightInset

        val boxWidth = maxOf(constraints.minWidth, occupiedSpaceHorizontally)

        return layout(boxWidth, boxHeight) {
            val placeableGroups =
                measuredPlaceable.groupBy { (measurable, _) ->
                    (measurable.parentData as? TitleBarChildDataNode)?.horizontalAlignment
                        ?: Alignment.CenterHorizontally
                }

            var headUsedSpace = leftInset
            var trailerUsedSpace = rightInset
            var centerUsedSpace = 0

            placeableGroups[Alignment.Start]?.forEach { (_, placeable) ->
                val x = headUsedSpace
                val y = Alignment.CenterVertically.align(placeable.height, boxHeight)
                placeable.placeRelative(x, y)
                headUsedSpace += placeable.width
            }
            placeableGroups[Alignment.End]?.forEach { (_, placeable) ->
                val x = boxWidth - placeable.width - trailerUsedSpace
                val y = Alignment.CenterVertically.align(placeable.height, boxHeight)
                placeable.placeRelative(x, y)
                trailerUsedSpace += placeable.width
            }

            val centerPlaceable = placeableGroups[Alignment.CenterHorizontally].orEmpty()

            val requiredCenterSpace = centerPlaceable.sumOf { it.second.width }
            val minX = headUsedSpace
            val maxX = boxWidth - trailerUsedSpace - requiredCenterSpace
            var centerX = (boxWidth - requiredCenterSpace) / 2

            if (minX <= maxX) {
                if (centerX > maxX) {
                    centerX = maxX
                }
                if (centerX < minX) {
                    centerX = minX
                }

                centerPlaceable.forEach { (_, placeable) ->
                    val x = centerX + centerUsedSpace
                    val y = Alignment.CenterVertically.align(placeable.height, boxHeight)
                    placeable.placeRelative(x, y)
                    centerUsedSpace += placeable.width
                }
            }

            applyContentWidth(
                if (headUsedSpace != 0) leftInset.toDp() to headUsedSpace.toDp()
                else null,
                if (centerUsedSpace != 0) centerX.toDp() to (centerX + centerUsedSpace).toDp()
                else null,
                if (trailerUsedSpace != 0) (boxWidth - trailerUsedSpace).toDp() to rightInset.toDp()
                else null,
            )
        }
    }
}

class TitleBarScopeImpl : TitleBarScope {

    override fun Modifier.align(alignment: Alignment.Horizontal): Modifier =
        this then TitleBarChildDataElement(
            alignment,
            debugInspectorInfo {
                name = "align"
                value = alignment
            },
        )
}

private class TitleBarChildDataElement(
    val horizontalAlignment: Alignment.Horizontal,
    val inspectorInfo: InspectorInfo.() -> Unit = NoInspectorInfo,
) : ModifierNodeElement<TitleBarChildDataNode>(), InspectableValue {

    override fun create(): TitleBarChildDataNode = TitleBarChildDataNode(horizontalAlignment)

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        val otherModifier = other as? TitleBarChildDataElement ?: return false
        return horizontalAlignment == otherModifier.horizontalAlignment
    }

    override fun hashCode(): Int = horizontalAlignment.hashCode()

    override fun update(node: TitleBarChildDataNode) {
        node.horizontalAlignment = horizontalAlignment
    }

    override fun InspectorInfo.inspectableProperties() {
        inspectorInfo()
    }
}

private class TitleBarChildDataNode(
    var horizontalAlignment: Alignment.Horizontal,
) : ParentDataModifierNode, Modifier.Node() {

    override fun Density.modifyParentData(parentData: Any?) =
        this@TitleBarChildDataNode
}

