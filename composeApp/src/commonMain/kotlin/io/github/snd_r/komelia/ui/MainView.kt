package io.github.snd_r.komelia.ui

import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.key.KeyEvent
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.unit.dp
import cafe.adriel.lyricist.ProvideStrings
import cafe.adriel.lyricist.rememberStrings
import cafe.adriel.voyager.navigator.Navigator
import coil3.PlatformContext
import coil3.compose.LocalPlatformContext
import com.dokar.sonner.ToastWidthPolicy
import com.dokar.sonner.Toaster
import com.dokar.sonner.ToasterState
import com.dokar.sonner.listenMany
import com.dokar.sonner.rememberToasterState
import io.github.snd_r.komelia.AppNotifications
import io.github.snd_r.komelia.ViewModelFactory
import io.github.snd_r.komelia.createViewModelFactory
import io.github.snd_r.komelia.platform.BackPressHandler
import io.github.snd_r.komelia.platform.WindowWidth
import io.github.snd_r.komelia.strings.EnStrings
import io.github.snd_r.komelia.strings.Locales
import io.github.snd_r.komelia.toToast
import io.github.snd_r.komelia.ui.common.AppTheme
import io.github.snd_r.komelia.ui.common.LoadingMaxSizeIndicator
import io.github.snd_r.komelia.ui.login.LoginScreen
import io.github.snd_r.komga.sse.KomgaEvent
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.util.*

val LocalViewModelFactory = compositionLocalOf<ViewModelFactory> { error("ViewModel factory is not set") }
val LocalToaster = compositionLocalOf<ToasterState> { error("Toaster is not set") }
val LocalKomgaEvents = compositionLocalOf<SharedFlow<KomgaEvent>> { error("Komga events are not set") }
val LocalKeyEvents = compositionLocalOf<SharedFlow<KeyEvent>> { error("Kev events are not set") }
val LocalWindowWidth = compositionLocalOf<WindowWidth> { error("Window size is not set") }
val LocalStrings = staticCompositionLocalOf { EnStrings }

val strings = mapOf(
    Locales.EN to EnStrings
)

private object ViewModelFactoryHolder {
    val instance: MutableStateFlow<ViewModelFactory?> = MutableStateFlow(null)
    private val mutex = Mutex()

    suspend fun createInstance(context: PlatformContext) {
        mutex.withLock {
            if (instance.value == null) instance.value = createViewModelFactory(context)
        }
    }
}

@Composable
fun MainView(
    windowWidth: WindowWidth,
    keyEvents: SharedFlow<KeyEvent>
) {
    MaterialTheme(colorScheme = AppTheme.dark) {
        val focusManager = LocalFocusManager.current
        val context = LocalPlatformContext.current
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(Unit) { detectTapGestures(onTap = { focusManager.clearFocus() }) }
        ) {
            val lyricist = rememberStrings(strings)
            ProvideStrings(lyricist, LocalStrings) {
                val notificationToaster = rememberToasterState()

                val viewModelFactory = ViewModelFactoryHolder.instance.collectAsState()
                LaunchedEffect(Unit) { ViewModelFactoryHolder.createInstance(context) }

                val actualViewModelFactory = viewModelFactory.value
                if (actualViewModelFactory != null) {
                    CompositionLocalProvider(
                        LocalViewModelFactory provides actualViewModelFactory,
                        LocalToaster provides notificationToaster,
                        LocalKomgaEvents provides actualViewModelFactory.getKomgaEvents(),
                        LocalKeyEvents provides keyEvents,
                        LocalWindowWidth provides windowWidth
                    ) {

                        Navigator(
                            screen = LoginScreen(),
                            onBackPressed = null
                        )
                        AppNotifications(actualViewModelFactory.getAppNotifications())
                    }
                } else LoadingMaxSizeIndicator()

                BackPressHandler {}
            }
        }
    }
}


@Composable
fun AppNotifications(
    appNotifications: AppNotifications,
) {
    val toaster = rememberToasterState(onToastDismissed = { appNotifications.remove(it.id as UUID) })

    LaunchedEffect(toaster) {
        val toastsFlow = appNotifications.getNotifications()
            .map { notifications -> notifications.map { it.toToast() } }
        toaster.listenMany(toastsFlow)
    }

    Toaster(
        state = toaster,
        richColors = true,
        darkTheme = true,
        showCloseButton = true,
        widthPolicy = { ToastWidthPolicy(max = 500.dp) },
        actionSlot = { toast ->
            when (toast.action) {
                null -> {}
                else -> {}
            }
        }
    )
}
