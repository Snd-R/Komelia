package io.github.snd_r.komelia

import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.awt.ComposeWindow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.window.WindowState

val LocalWindow = compositionLocalOf<ComposeWindow> { error("Compose window was not set") }
val LocalWindowState = compositionLocalOf<WindowState> { error("Window state was not set") }
val LocalDesktopViewModelFactory = compositionLocalOf<DesktopViewModelFactory?> {
    error("DesktopViewModel factory is not set")
}
val windowBorder = mutableStateOf(Color.Unspecified)
