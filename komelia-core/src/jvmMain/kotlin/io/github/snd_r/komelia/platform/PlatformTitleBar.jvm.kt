package io.github.snd_r.komelia.platform

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.heightIn
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.awt.ComposeWindow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.WindowPlacement
import com.jetbrains.JBR
import io.github.snd_r.komelia.DesktopPlatform
import io.github.snd_r.komelia.DesktopPlatform.Linux
import io.github.snd_r.komelia.DesktopPlatform.MacOS
import io.github.snd_r.komelia.DesktopPlatform.Unknown
import io.github.snd_r.komelia.DesktopPlatform.Windows
import io.github.snd_r.komelia.LocalWindow
import io.github.snd_r.komelia.window.TitleBarOnLinux
import io.github.snd_r.komelia.window.TitleBarOnWindows
import java.awt.event.ComponentEvent
import java.awt.event.ComponentListener
import java.awt.event.WindowAdapter
import java.awt.event.WindowEvent

val csdEnvEnabled = System.getenv("USE_CSD")?.toBoolean() ?: true
actual fun canIntegrateWithSystemBar() = JBR.isAvailable() && csdEnvEnabled

@Composable
actual fun PlatformTitleBar(
    modifier: Modifier,
    applyInsets: Boolean,
    content: @Composable TitleBarScope.() -> Unit,
) {
    val window = LocalWindow.current
    var decoratedWindowState by remember { mutableStateOf(DecoratedWindowState.of(window)) }

    DisposableEffect(window) {
        val adapter = object : WindowAdapter(), ComponentListener {
            override fun windowActivated(e: WindowEvent?) {
                decoratedWindowState = DecoratedWindowState.of(window)
            }

            override fun windowDeactivated(e: WindowEvent?) {
                decoratedWindowState = DecoratedWindowState.of(window)
            }

            override fun windowIconified(e: WindowEvent?) {
                decoratedWindowState = DecoratedWindowState.of(window)
            }

            override fun windowDeiconified(e: WindowEvent?) {
                decoratedWindowState = DecoratedWindowState.of(window)
            }

            override fun windowStateChanged(e: WindowEvent) {
                decoratedWindowState = DecoratedWindowState.of(window)
            }

            override fun componentResized(e: ComponentEvent?) {
                decoratedWindowState = DecoratedWindowState.of(window)
            }

            override fun componentMoved(e: ComponentEvent?) {
                // Empty
            }

            override fun componentShown(e: ComponentEvent?) {
                // Empty
            }

            override fun componentHidden(e: ComponentEvent?) {
                // Empty
            }
        }

        window.addWindowListener(adapter)
        window.addWindowStateListener(adapter)
        window.addComponentListener(adapter)

        onDispose {
            window.removeWindowListener(adapter)
            window.removeWindowStateListener(adapter)
            window.removeComponentListener(adapter)
        }
    }

    if (!canIntegrateWithSystemBar() || decoratedWindowState.isFullscreen) {
        SimpleTitleBarLayout(modifier, applyInsets, content)
    } else {
        when (DesktopPlatform.Current) {
            Windows -> TitleBarOnWindows(
                modifier.heightIn(min = 32.dp).background(MaterialTheme.colorScheme.surfaceDim),
                window,
                content
            )

            Linux -> TitleBarOnLinux(
                modifier.heightIn(min = 32.dp).background(MaterialTheme.colorScheme.surfaceDim),
                window,
                decoratedWindowState,
                content
            )

            MacOS, Unknown -> SimpleTitleBarLayout(modifier, applyInsets, content)
        }
    }
}

@Immutable
@JvmInline
value class DecoratedWindowState(val state: ULong) {

    val isActive: Boolean
        get() = state and Active != 0UL

    val isFullscreen: Boolean
        get() = state and Fullscreen != 0UL

    val isMinimized: Boolean
        get() = state and Minimize != 0UL

    val isMaximized: Boolean
        get() = state and Maximize != 0UL

    fun copy(
        fullscreen: Boolean = isFullscreen,
        minimized: Boolean = isMinimized,
        maximized: Boolean = isMaximized,
        active: Boolean = isActive,
    ): DecoratedWindowState =
        of(
            fullscreen = fullscreen,
            minimized = minimized,
            maximized = maximized,
            active = active,
        )

    override fun toString(): String =
        "${javaClass.simpleName}(isFullscreen=$isFullscreen, isActive=$isActive)"

    companion object {

        val Active: ULong = 1UL shl 0
        val Fullscreen: ULong = 1UL shl 1
        val Minimize: ULong = 1UL shl 2
        val Maximize: ULong = 1UL shl 3

        fun of(
            fullscreen: Boolean = false,
            minimized: Boolean = false,
            maximized: Boolean = false,
            active: Boolean = true,
        ): DecoratedWindowState =
            DecoratedWindowState(
                (if (fullscreen) Fullscreen else 0UL) or
                        (if (minimized) Minimize else 0UL) or
                        (if (maximized) Maximize else 0UL) or
                        (if (active) Active else 0UL),
            )

        fun of(window: ComposeWindow): DecoratedWindowState =
            of(
                fullscreen = window.placement == WindowPlacement.Fullscreen,
                minimized = window.isMinimized,
                maximized = window.placement == WindowPlacement.Maximized,
                active = window.isActive,
            )
    }
}
