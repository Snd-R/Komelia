package io.github.snd_r.komelia.ui

import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.input.key.KeyEvent
import com.dokar.sonner.ToasterState
import io.github.snd_r.komelia.ViewModelFactory
import io.github.snd_r.komelia.platform.AppWindowState
import io.github.snd_r.komelia.platform.PlatformType
import io.github.snd_r.komelia.platform.WindowSizeClass
import io.github.snd_r.komelia.strings.EnStrings
import io.github.snd_r.komelia.ui.common.AppTheme
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flowOf
import snd.komga.client.library.KomgaLibrary
import snd.komga.client.sse.KomgaEvent

val LocalViewModelFactory = compositionLocalOf<ViewModelFactory> { error("ViewModel factory is not set") }
val LocalToaster = compositionLocalOf<ToasterState> { error("Toaster is not set") }
val LocalKomgaEvents = compositionLocalOf<SharedFlow<KomgaEvent>> { error("Komga events are not set") }
val LocalKomfIntegration = compositionLocalOf { flowOf(false) }
val LocalKeyEvents = compositionLocalOf<SharedFlow<KeyEvent>> { error("Key events are not set") }
val LocalWindowWidth = compositionLocalOf<WindowSizeClass> { error("Window size is not set") }
val LocalWindowHeight = compositionLocalOf<WindowSizeClass> { error("Window size is not set") }
val LocalStrings = staticCompositionLocalOf { EnStrings }
val LocalPlatform = compositionLocalOf<PlatformType> { error("Platform type is not set") }
val LocalTheme = compositionLocalOf { AppTheme.DARK }
val LocalWindowState = compositionLocalOf<AppWindowState> { error("Window state was not initialized") }
val LocalLibraries = compositionLocalOf<StateFlow<List<KomgaLibrary>>> { error("Libraries were not initialized") }
val LocalReloadEvents = staticCompositionLocalOf<SharedFlow<Unit>> { error("Reload event flow was not initialized") }
