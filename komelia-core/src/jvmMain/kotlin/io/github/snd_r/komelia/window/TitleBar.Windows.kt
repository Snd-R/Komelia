package io.github.snd_r.komelia.window

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.awt.ComposeWindow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.unit.dp
import com.jetbrains.JBR
import com.jetbrains.WindowDecorations.CustomTitleBar
import io.github.snd_r.komelia.platform.TitleBarLayout
import io.github.snd_r.komelia.platform.TitleBarScope
import io.github.snd_r.komelia.ui.LocalTheme
import io.github.snd_r.komelia.ui.common.AppTheme
import java.awt.event.MouseAdapter
import java.awt.event.MouseEvent

@Composable
internal fun TitleBarOnWindows(
    modifier: Modifier = Modifier,
    window: ComposeWindow,
    content: @Composable TitleBarScope.() -> Unit,
) {
    val theme = LocalTheme.current
    val titleBar = remember { JBR.getWindowDecorations().createCustomTitleBar() }
    val titleBarClientHitAdapter = remember { ClientAreaHitAdapter(titleBar) }

    TitleBarLayout(
        modifier = modifier,
        applyTitleBar = { height ->
            titleBar.height = height.value
            titleBar.putProperty("controls.dark", theme == AppTheme.DARK)
            titleBar.putProperty("controls.visible", true)

            JBR.getWindowDecorations().setCustomTitleBar(window, titleBar)
            PaddingValues(start = titleBar.leftInset.dp, end = titleBar.rightInset.dp)
        },
        onElementsPlaced = { elements -> titleBarClientHitAdapter.elements = elements },
        content = content,
    )

    DisposableEffect(Unit) {
        window.addMouseListener(titleBarClientHitAdapter)
        window.addMouseMotionListener(titleBarClientHitAdapter)

        onDispose {
            window.removeMouseListener(titleBarClientHitAdapter)
            window.removeMouseMotionListener(titleBarClientHitAdapter)

            // crashes on app exit if window placement is accessed here?
            // parent DecoratedWindowState listener crashes with NPE trying to access parent window to check placement
//            if (window.placement == WindowPlacement.Fullscreen)
            titleBar.putProperty("controls.visible", false)
        }

    }
}

class ClientAreaHitAdapter(private val titleBar: CustomTitleBar) : MouseAdapter() {
    var elements: List<Rect> = emptyList()

    private fun hit(event: MouseEvent) {
        if (event.y > titleBar.height) titleBar.forceHitTest(false)
        val offset = Offset(event.x.toFloat(), event.y.toFloat())
        val isClientArea = elements.any { it.contains(offset) }
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
