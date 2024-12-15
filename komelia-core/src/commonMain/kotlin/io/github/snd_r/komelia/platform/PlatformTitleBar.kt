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
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.layout.Measurable
import androidx.compose.ui.layout.MeasurePolicy
import androidx.compose.ui.layout.MeasureResult
import androidx.compose.ui.layout.MeasureScope
import androidx.compose.ui.layout.Measured
import androidx.compose.ui.layout.Placeable
import androidx.compose.ui.node.ModifierNodeElement
import androidx.compose.ui.node.ParentDataModifierNode
import androidx.compose.ui.platform.InspectorInfo
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.offset
import kotlin.math.max

expect fun canIntegrateWithSystemBar(): Boolean

@Composable
expect fun PlatformTitleBar(
    modifier: Modifier = Modifier,
    applyInsets: Boolean = true,
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
            onElementsPlaced = { _ -> },
            content = content
        )

    }

}

@Composable
fun TitleBarLayout(
    modifier: Modifier = Modifier,
    applyTitleBar: (height: Dp) -> PaddingValues,
    onElementsPlaced: (elements: List<Rect>) -> Unit,
    content: @Composable TitleBarScope.() -> Unit,
) {
    Layout(
        content = { TitleBarScopeImpl().content() },
        modifier = modifier.fillMaxWidth(),
        measurePolicy = remember(applyTitleBar) {
            TitleBarMeasurePolicy(
                applyTitleBar,
                onElementsPlaced
            )
        }
    )
}

interface TitleBarScope {
    @Stable
    fun Modifier.align(alignment: Alignment.Horizontal): Modifier

    fun Modifier.nonInteractive(): Modifier
}

internal class TitleBarMeasurePolicy(
    private val applyTitleBar: (height: Dp) -> PaddingValues,
    private val onElementsPlaced: (elements: List<Rect>) -> Unit,
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

        val placedElements = mutableListOf<Rect>()
        return layout(boxWidth, boxHeight) {
            val placeableGroups =
                measuredPlaceable.groupBy { (measurable, _) ->
                    (measurable.parentData as? TitleBarParentData)?.horizontalAlignment
                        ?: Alignment.CenterHorizontally
                }

            var headUsedSpace = leftInset
            var trailerUsedSpace = rightInset
            var centerUsedSpace = 0

            placeableGroups[Alignment.Start]?.forEach { (_, placeable) ->
                val x = headUsedSpace
                val y = Alignment.CenterVertically.align(placeable.height, boxHeight)
                placeable.placeRelative(x, y)
                if (placeable.isInteractable()) {
                    placedElements.add(
                        Rect(
                            left = x.toDp().value,
                            right = (x + placeable.width).toDp().value,
                            top = y.toDp().value,
                            bottom = placeable.height.toDp().value
                        )
                    )
                }
                headUsedSpace += placeable.width
            }
            placeableGroups[Alignment.End]?.forEach { (_, placeable) ->
                val x = boxWidth - placeable.width - trailerUsedSpace
                val y = Alignment.CenterVertically.align(placeable.height, boxHeight)
                placeable.placeRelative(x, y)
                if (placeable.isInteractable()) {
                    placedElements.add(
                        Rect(
                            left = x.toDp().value,
                            right = (x + placeable.width).toDp().value,
                            top = y.toDp().value,
                            bottom = placeable.height.toDp().value
                        )
                    )
                }
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
                    if (placeable.isInteractable()) {
                        placedElements.add(
                            Rect(
                                left = x.toDp().value,
                                right = (x + placeable.width).toDp().value,
                                top = y.toDp().value,
                                bottom = placeable.height.toDp().value
                            )
                        )
                    }
                    centerUsedSpace += placeable.width
                }
            }

            onElementsPlaced(placedElements)
        }
    }
}

private fun Measured.isInteractable() = (this.parentData as? TitleBarParentData)?.interactable ?: true

data class TitleBarElementPlacement(
    val start: Dp,
    val end: Dp,
    val top: Dp,
    val bottom: Dp
)

class TitleBarScopeImpl : TitleBarScope {
    override fun Modifier.align(alignment: Alignment.Horizontal): Modifier =
        this then TitleBarAlignmentElement(alignment)

    override fun Modifier.nonInteractive(): Modifier =
        this then TitleBarNonInteractiveElement()
}

private class TitleBarAlignmentElement(
    val horizontalAlignment: Alignment.Horizontal,
) : ModifierNodeElement<TitleBarAlignmentNode>() {

    override fun create(): TitleBarAlignmentNode = TitleBarAlignmentNode(horizontalAlignment)

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        val otherModifier = other as? TitleBarAlignmentElement ?: return false
        return horizontalAlignment == otherModifier.horizontalAlignment
    }

    override fun hashCode(): Int = horizontalAlignment.hashCode()

    override fun update(node: TitleBarAlignmentNode) {
        node.horizontalAlignment = horizontalAlignment
    }

    override fun InspectorInfo.inspectableProperties() {
        name = "align"
        value = horizontalAlignment
    }
}

private class TitleBarAlignmentNode(
    var horizontalAlignment: Alignment.Horizontal,
) : ParentDataModifierNode, Modifier.Node() {

    override fun Density.modifyParentData(parentData: Any?) =
        ((parentData as? TitleBarParentData) ?: TitleBarParentData()).also {
            it.horizontalAlignment = horizontalAlignment
        }
}

private class TitleBarNonInteractiveElement : ModifierNodeElement<TitleBarNonInteractiveNode>() {
    override fun create() = TitleBarNonInteractiveNode()
    override fun equals(other: Any?): Boolean = this === other
    override fun hashCode(): Int = 0

    override fun update(node: TitleBarNonInteractiveNode) = Unit
}

private class TitleBarNonInteractiveNode : ParentDataModifierNode, Modifier.Node() {
    override fun Density.modifyParentData(parentData: Any?) =
        ((parentData as? TitleBarParentData) ?: TitleBarParentData()).also {
            it.interactable = false
        }
}

internal data class TitleBarParentData(
    var horizontalAlignment: Alignment.Horizontal = Alignment.CenterHorizontally,
    var interactable: Boolean = true
)
