package snd.komelia.ui.platform

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CropSquare
import androidx.compose.material.icons.filled.FilterNone
import androidx.compose.material.icons.filled.Minimize
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.awt.ComposeWindow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.PointerButton
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.input.pointer.onPointerEvent
import androidx.compose.ui.platform.LocalViewConfiguration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.WindowPlacement
import com.jetbrains.JBR
import snd.komelia.AwtWindowState
import java.awt.Frame
import java.awt.event.MouseEvent
import java.awt.event.WindowEvent

@OptIn(ExperimentalComposeUiApi::class)
@Composable
internal fun TitleBarOnLinux(
    modifier: Modifier = Modifier,
    window: ComposeWindow,
    windowState: AwtWindowState,
    content: @Composable TitleBarScope.() -> Unit,
) {
    var lastPress = 0L
    val viewConfig = LocalViewConfiguration.current

    TitleBarLayout(
        modifier = modifier
            .onPointerEvent(PointerEventType.Press, PointerEventPass.Main) {
                if (this.currentEvent.button == PointerButton.Primary &&
                    this.currentEvent.changes.any { changed -> !changed.isConsumed }
                ) {
                    JBR.getWindowMove()?.startMovingTogetherWithMouse(window, MouseEvent.BUTTON1)
                    if (System.currentTimeMillis() - lastPress in
                        viewConfig.doubleTapMinTimeMillis..viewConfig.doubleTapTimeoutMillis
                    ) {
                        if (window.placement == WindowPlacement.Maximized) {
                            window.extendedState = Frame.NORMAL
                        } else {
                            window.extendedState = Frame.MAXIMIZED_BOTH
                        }
                    }
                    lastPress = System.currentTimeMillis()
                }
            },
        applyTitleBar = { _ -> PaddingValues(0.dp) },
        onElementsPlaced = { _ -> }
    ) {
        ControlButton(
            { window.dispatchEvent(WindowEvent(window, WindowEvent.WINDOW_CLOSING)) },
            Icons.Default.Close,
            true
        )

        if (windowState.placement == WindowPlacement.Maximized) {
            ControlButton(
                { window.extendedState = Frame.NORMAL },
                Icons.Default.FilterNone,
                true
            )
        } else {
            ControlButton(
                { window.extendedState = Frame.MAXIMIZED_BOTH },
                Icons.Default.CropSquare,
                true
            )
        }
        ControlButton(
            { window.extendedState = Frame.ICONIFIED },
            Icons.Default.Minimize,
            true
        )
        content()
    }
}

@Composable
private fun TitleBarScope.ControlButton(
    onClick: () -> Unit,
    icon: ImageVector,
    isActive: Boolean,
) {
    Icon(
        icon, null,
        tint = if (isActive) LocalContentColor.current else Color.Gray,
        modifier = Modifier
            .clickable { onClick() }
            .padding(horizontal = 15.dp, vertical = 8.dp)
            .size(18.dp)
            .align(Alignment.End)
    )
}
