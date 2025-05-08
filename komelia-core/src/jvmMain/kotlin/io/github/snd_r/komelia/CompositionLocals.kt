package io.github.snd_r.komelia

import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.awt.ComposeWindow
import androidx.compose.ui.graphics.Color
import io.github.snd_r.komelia.platform.AwtWindowState

val LocalWindowState = compositionLocalOf<AwtWindowState> { error("window state composition local was not set") }
val LocalWindow = compositionLocalOf<ComposeWindow> { error("Compose window was not set") }
val windowBorder = mutableStateOf(Color.Unspecified)
