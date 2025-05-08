package io.github.snd_r.komelia.platform

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.awt.ComposeWindow
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.WindowPlacement
import androidx.compose.ui.window.WindowPlacement.Floating
import androidx.compose.ui.window.WindowPlacement.Fullscreen
import androidx.compose.ui.window.WindowPosition
import androidx.compose.ui.window.WindowState
import com.sun.jna.Pointer
import com.sun.jna.platform.win32.User32
import com.sun.jna.platform.win32.WinDef
import com.sun.jna.platform.win32.WinUser
import io.github.snd_r.komelia.DesktopPlatform
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn


class AwtWindowState(private val composeWindow: Flow<ComposeWindow>) : AppWindowState, WindowState {
    private val coroutineScope = CoroutineScope(Dispatchers.Default + SupervisorJob())

    override var placement by mutableStateOf(Floating)
    override var isMinimized by mutableStateOf(false)
    override var position: WindowPosition by mutableStateOf(WindowPosition.PlatformDefault)
    override var size by mutableStateOf(DpSize(1280.dp, 720.dp))
    override val isFullscreen get() = snapshotFlow { placement }.map { it == Fullscreen }

    private val fullscreenStrategy = MutableStateFlow<FullscreenStrategy>(
        DefaultFullscreenStrategy(
            placement = snapshotFlow { placement }.stateIn(coroutineScope, SharingStarted.Eagerly, placement),
            onPlacementChange = { placement = it })
    )

    init {
        composeWindow.onEach {
            if (DesktopPlatform.Current == DesktopPlatform.Windows) {
                fullscreenStrategy.value = WindowsFullscreenStrategy(
                    window = composeWindow.first(),
                    onPlacementChange = { placement = it },
                )
            } else if (DesktopPlatform.Current == DesktopPlatform.Linux) {
                fullscreenStrategy.value = LinuxFullscreenStrategy(onPlacementChange = { placement = it })
            }
        }.launchIn(coroutineScope)
    }

    override fun setFullscreen(enabled: Boolean) {
        fullscreenStrategy.value.setFullscreen(enabled)
    }
}

private sealed interface FullscreenStrategy {
    fun setFullscreen(enabled: Boolean)
}

private class DefaultFullscreenStrategy(
    private val placement: StateFlow<WindowPlacement>,
    private val onPlacementChange: (WindowPlacement) -> Unit,
) : FullscreenStrategy {
    @Volatile
    private var placementBeforeFullscreen = Floating
    override fun setFullscreen(enabled: Boolean) {
        if (enabled) {
            placementBeforeFullscreen = placement.value
            onPlacementChange(Fullscreen)
        } else {
            onPlacementChange(placementBeforeFullscreen)
        }
    }
}

private class WindowsFullscreenStrategy(
    window: ComposeWindow,
    private val onPlacementChange: (WindowPlacement) -> Unit,
) : FullscreenStrategy {

    @Volatile
    private var styleBeforeFullscreen = Pointer.NULL
    private val undecoratedStyle = Pointer.createConstant(0x970B0000)
    private val hwnd = WinDef.HWND(Pointer(window.windowHandle))

    override fun setFullscreen(enabled: Boolean) {
        if (enabled) {
            styleBeforeFullscreen = User32.INSTANCE.GetWindowLongPtr(hwnd, WinUser.GWL_STYLE).toPointer()
            User32.INSTANCE.SetWindowLongPtr(hwnd, WinUser.GWL_STYLE, undecoratedStyle)

            onPlacementChange(Fullscreen)
        } else {
            User32.INSTANCE.SetWindowLongPtr(hwnd, WinUser.GWL_STYLE, styleBeforeFullscreen)
            onPlacementChange(Floating)
        }
    }
}

private class LinuxFullscreenStrategy(
    private val onPlacementChange: (WindowPlacement) -> Unit
) : FullscreenStrategy {
    override fun setFullscreen(enabled: Boolean) {
        if (enabled) {
            onPlacementChange(Fullscreen)
        } else {
            onPlacementChange(Floating)
        }
    }
}
