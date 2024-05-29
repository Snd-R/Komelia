package io.github.snd_r.komelia.window

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
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
import com.jetbrains.JBR
import io.github.snd_r.komelia.platform.DecoratedWindowState
import io.github.snd_r.komelia.platform.TitleBarLayout
import io.github.snd_r.komelia.platform.TitleBarScope
import java.awt.Frame
import java.awt.event.MouseEvent
import java.awt.event.WindowAdapter
import java.awt.event.WindowEvent

@OptIn(ExperimentalComposeUiApi::class)
@Composable
internal fun TitleBarOnLinux(
    modifier: Modifier = Modifier,
    window: ComposeWindow,
    state: DecoratedWindowState,
    content: @Composable TitleBarScope.() -> Unit,
) {
    var lastPress = 0L
    val viewConfig = LocalViewConfiguration.current
    var isActive by remember { mutableStateOf(true) }
    DisposableEffect(window) {
        val adapter = object : WindowAdapter() {
            override fun windowGainedFocus(e: WindowEvent?) {
                isActive = true
            }

            override fun windowLostFocus(e: WindowEvent?) {
                isActive = false
            }
        }

        window.addWindowFocusListener(adapter)
        onDispose { window.removeWindowFocusListener(adapter) }
    }


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
                        if (state.isMaximized) {
                            window.extendedState = Frame.NORMAL
                        } else {
                            window.extendedState = Frame.MAXIMIZED_BOTH
                        }
                    }
                    lastPress = System.currentTimeMillis()
                }
            },
        applyTitleBar = { _ -> PaddingValues(0.dp) },
        applyContentWidth = { _, _, _ -> }
    ) {
        ControlButton(
            { window.dispatchEvent(WindowEvent(window, WindowEvent.WINDOW_CLOSING)) },
            Icons.Default.Close,
            isActive,
        )

        if (state.isMaximized) {
            ControlButton(
                { window.extendedState = Frame.NORMAL },
                Icons.Default.FilterNone,
                isActive,
            )
        } else {
            ControlButton(
                { window.extendedState = Frame.MAXIMIZED_BOTH },
                Icons.Default.CropSquare,
                isActive,
            )
        }
        ControlButton(
            { window.extendedState = Frame.ICONIFIED },
            Icons.Default.Minimize,
            isActive,
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
