package io.github.snd_r.komelia.window

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.awt.ComposeWindow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.WindowPlacement
import com.jetbrains.JBR
import com.jetbrains.WindowDecorations.CustomTitleBar
import io.github.snd_r.komelia.platform.TitleBarLayout
import io.github.snd_r.komelia.platform.TitleBarScope
import java.awt.event.MouseAdapter
import java.awt.event.MouseEvent

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
            titleBar.putProperty("controls.visible", true)

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
            if (window.placement == WindowPlacement.Fullscreen) titleBar.putProperty("controls.visible", false)
        }

    }
}

class ClientAreaHitAdapter(private val titleBar: CustomTitleBar) : MouseAdapter() {
    var startSpace: ClosedFloatingPointRange<Float>? = null
    var centerSpace: ClosedFloatingPointRange<Float>? = null
    var endSpace: ClosedFloatingPointRange<Float>? = null

    private fun hit(e: MouseEvent) {
        val x = e.x.toFloat()
        val isClientArea = e.y <= titleBar.height &&
                (startSpace?.contains(x) == true || centerSpace?.contains(x) == true || endSpace?.contains(x) == true)

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
