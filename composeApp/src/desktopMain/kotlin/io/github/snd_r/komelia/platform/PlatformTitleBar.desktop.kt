package io.github.snd_r.komelia.platform

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.awt.ComposeWindow
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
import com.jetbrains.JBR
import com.jetbrains.WindowDecorations.CustomTitleBar
import io.github.snd_r.komelia.DesktopPlatform
import io.github.snd_r.komelia.DesktopPlatform.Linux
import io.github.snd_r.komelia.DesktopPlatform.MacOS
import io.github.snd_r.komelia.DesktopPlatform.Unknown
import io.github.snd_r.komelia.DesktopPlatform.Windows
import io.github.snd_r.komelia.LocalWindow
import java.awt.event.MouseAdapter
import java.awt.event.MouseEvent
import kotlin.math.max

@Composable
actual fun PlatformTitleBar(
    content: @Composable TitleBarScope.() -> Unit,
) {

    if (!JBR.isAvailable()) {
        TitleBarLayout(
            modifier = Modifier,
            applyTitleBar = { _ -> PaddingValues(0.dp) },
            applyContentWidth = { _, _, _ -> },
            content = content
        )
    } else {
        val window = LocalWindow.current
        when (DesktopPlatform.Current) {
            Windows -> TitleBarOnWindows(Modifier, window, content)
            Linux, MacOS, Unknown -> error("TitleBar is not supported on this platform(${System.getProperty("os.name")})")
        }
    }
}

@Composable
internal fun TitleBarOnWindows(
    modifier: Modifier = Modifier,
    window: ComposeWindow,
    content: @Composable TitleBarScope.() -> Unit,
) {
    val titleBar = remember { JBR.getWindowDecorations().createCustomTitleBar() }
    val titleBarClientHitAdapter = remember { ClientAreaHitAdapter(titleBar) }

    TitleBarLayout(
        modifier = modifier,
        applyTitleBar = { height ->
            titleBar.height = height.value
            titleBar.putProperty("controls.dark", true)

            JBR.getWindowDecorations().setCustomTitleBar(window, titleBar)
            PaddingValues(start = titleBar.leftInset.dp, end = titleBar.rightInset.dp)
        },
        applyContentWidth = { start, center, end ->
            titleBarClientHitAdapter.startSpace = start?.let { (start, end) -> start.value..end.value }
            titleBarClientHitAdapter.centerSpace = center?.let { (start, end) -> start.value..end.value }
            titleBarClientHitAdapter.endSpace = end?.let { (start, end) -> start.value..end.value }

        },
        content = content,
    )

    DisposableEffect(Unit) {
        window.addMouseListener(titleBarClientHitAdapter)
        window.addMouseMotionListener(titleBarClientHitAdapter)

        onDispose {
            window.removeMouseListener(titleBarClientHitAdapter)
            window.removeMouseMotionListener(titleBarClientHitAdapter)
        }

    }
}

@Composable
internal fun TitleBarLayout(
    modifier: Modifier = Modifier,
    applyTitleBar: (height: Dp) -> PaddingValues,
    applyContentWidth: (start: Pair<Dp, Dp>?, center: Pair<Dp, Dp>?, end: Pair<Dp, Dp>?) -> Unit,
    content: @Composable TitleBarScope.() -> Unit,
) {
    Layout(
        content = { TitleBarScopeImpl().content() },
        modifier = modifier.fillMaxWidth(),
        measurePolicy = remember(applyTitleBar) { TitleBarMeasurePolicy(applyTitleBar, applyContentWidth) }
    )
}

internal class TitleBarMeasurePolicy(
    private val applyTitleBar: (height: Dp) -> PaddingValues,
    private val applyContentWidth: (start: Pair<Dp, Dp>?, center: Pair<Dp, Dp>?, end: Pair<Dp, Dp>?) -> Unit,
) : MeasurePolicy {

    override fun MeasureScope.measure(measurables: List<Measurable>, constraints: Constraints): MeasureResult {
        if (measurables.isEmpty()) {
            return layout(width = constraints.minWidth, height = constraints.minHeight) {}
        }

        var occupiedSpaceHorizontally = 0

        var maxSpaceVertically = constraints.minHeight
        val contentConstraints = constraints.copy(minWidth = 0, minHeight = 0)
        val measuredPlaceable = mutableListOf<Pair<Measurable, Placeable>>()

        for (it in measurables) {
            val placeable = it.measure(contentConstraints.offset(horizontal = -occupiedSpaceHorizontally))
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

class ClientAreaHitAdapter(private val titleBar: CustomTitleBar) : MouseAdapter() {
    var startSpace: ClosedFloatingPointRange<Float>? = null
    var centerSpace: ClosedFloatingPointRange<Float>? = null
    var endSpace: ClosedFloatingPointRange<Float>? = null

    private fun hit(e: MouseEvent) {
        val x = e.x.toFloat()
        val isClientArea = e.y <= titleBar.height && (
                startSpace?.contains(x) == true
                        || centerSpace?.contains(x) == true
                        || endSpace?.contains(x) == true)

        titleBar.forceHitTest(isClientArea)
    }

    override fun mouseClicked(e: MouseEvent) {
        hit(e)
    }

    override fun mousePressed(e: MouseEvent) {
        hit(e)
    }

    override fun mouseReleased(e: MouseEvent) {
        hit(e)
    }

    override fun mouseEntered(e: MouseEvent) {
        hit(e)
    }

    override fun mouseExited(e: MouseEvent) {
        hit(e)
    }

    override fun mouseDragged(e: MouseEvent) {
        hit(e)
    }

    override fun mouseMoved(e: MouseEvent) {
        hit(e)
    }
}

private class TitleBarScopeImpl : TitleBarScope {

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
