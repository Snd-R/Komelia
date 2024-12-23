package io.github.snd_r.komelia.curves

import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.key.KeyEvent
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalFocusManager
import cafe.adriel.voyager.navigator.Navigator
import com.dokar.sonner.rememberToasterState
import io.github.snd_r.komelia.DependencyContainer
import io.github.snd_r.komelia.ViewModelFactory
import io.github.snd_r.komelia.platform.BackPressHandler
import io.github.snd_r.komelia.platform.ConfigurePlatformTheme
import io.github.snd_r.komelia.platform.PlatformTitleBar
import io.github.snd_r.komelia.platform.PlatformType
import io.github.snd_r.komelia.platform.WindowSizeClass
import io.github.snd_r.komelia.ui.AppNotifications
import io.github.snd_r.komelia.ui.LocalKeyEvents
import io.github.snd_r.komelia.ui.LocalKomfIntegration
import io.github.snd_r.komelia.ui.LocalKomgaEvents
import io.github.snd_r.komelia.ui.LocalPlatform
import io.github.snd_r.komelia.ui.LocalTheme
import io.github.snd_r.komelia.ui.LocalToaster
import io.github.snd_r.komelia.ui.LocalViewModelFactory
import io.github.snd_r.komelia.ui.LocalWindowHeight
import io.github.snd_r.komelia.ui.LocalWindowState
import io.github.snd_r.komelia.ui.LocalWindowWidth
import io.github.snd_r.komelia.ui.common.AppTheme
import io.github.snd_r.komelia.ui.common.LoadingMaxSizeIndicator
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow

private val vmFactory = MutableStateFlow<ViewModelFactory?>(null)

@Composable
fun CurvesView(
    dependencies: DependencyContainer?,
    windowWidth: WindowSizeClass,
    windowHeight: WindowSizeClass,
    platformType: PlatformType,
    keyEvents: SharedFlow<KeyEvent>
) {
    val theme by rememberSaveable { mutableStateOf(AppTheme.LIGHT) }

    MaterialTheme(colorScheme = theme.colorScheme) {
        ConfigurePlatformTheme(theme)
        val focusManager = LocalFocusManager.current
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(Unit) { detectTapGestures(onTap = { focusManager.clearFocus() }) }
        ) {
            if (dependencies == null) {
                Column {
                    PlatformTitleBar { }
                    LoadingMaxSizeIndicator()
                }
                return@Surface
            }

            val viewModelFactory = vmFactory.collectAsState().value
            LaunchedEffect(Unit) {
                if (vmFactory.value == null) {
                    vmFactory.value = ViewModelFactory(dependencies, platformType)
                }
            }

            if (viewModelFactory == null) return@Surface

            val notificationToaster = rememberToasterState()

            CompositionLocalProvider(
                LocalViewModelFactory provides viewModelFactory,
                LocalToaster provides notificationToaster,
                LocalKomgaEvents provides viewModelFactory.getKomgaEvents(),
                LocalKomfIntegration provides dependencies.settingsRepository.getKomfEnabled(),
                LocalKeyEvents provides keyEvents,
                LocalPlatform provides platformType,
                LocalTheme provides theme,
                LocalWindowState provides dependencies.windowState,
                LocalWindowWidth provides windowWidth,
                LocalWindowHeight provides windowHeight,
            ) {

                Navigator(
                    screen = CurvesScreen(),
                    onBackPressed = null
                )

                AppNotifications(dependencies.appNotifications, theme)

            }

            BackPressHandler {}
        }
    }
}