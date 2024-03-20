package io.github.snd_r.komelia.ui

import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.key.KeyEvent
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.navigator.Navigator
import com.dokar.sonner.ToastWidthPolicy
import com.dokar.sonner.Toaster
import com.dokar.sonner.ToasterState
import com.dokar.sonner.listenMany
import com.dokar.sonner.rememberToasterState
import io.github.oshai.kotlinlogging.KotlinLogging
import io.github.snd_r.komelia.AppNotifications
import io.github.snd_r.komelia.ViewModelFactory
import io.github.snd_r.komelia.createViewModelFactory
import io.github.snd_r.komelia.toToast
import io.github.snd_r.komelia.ui.common.CustomTheme
import io.github.snd_r.komelia.ui.common.LoadingMaxSizeIndicator
import io.github.snd_r.komelia.ui.login.LoginScreen
import io.github.snd_r.komga.sse.KomgaEvent
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.map
import java.util.*

val LocalViewModelFactory = compositionLocalOf<ViewModelFactory> { error("ViewModel factory is not set") }
val LocalToaster = compositionLocalOf<ToasterState> { error("Toaster is not set") }
val LocalKomgaEvents = compositionLocalOf<SharedFlow<KomgaEvent>> { error("Komga events are not set") }
val LocalKeyEvents = compositionLocalOf<SharedFlow<KeyEvent>> { error("Kev events are not set") }

private val logger = KotlinLogging.logger {}

@Composable
fun MainView(
    windowHeight: Dp,
    windowWidth: Dp,
    keyEvents: SharedFlow<KeyEvent>
) {
    CustomTheme(windowHeight, windowWidth) {
        val focusManager = LocalFocusManager.current
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(Unit) { detectTapGestures(onTap = { focusManager.clearFocus() }) }
        ) {
            val notificationToaster = rememberToasterState()

            val viewModelFactory = remember { mutableStateOf<ViewModelFactory?>(null) }
            LaunchedEffect(Unit) {
                viewModelFactory.value = createViewModelFactory(this)
            }
            val actualViewModelFactory = viewModelFactory.value

            if (actualViewModelFactory != null) {
                CompositionLocalProvider(
                    LocalViewModelFactory provides actualViewModelFactory,
                    LocalToaster provides notificationToaster,
                    LocalKomgaEvents provides actualViewModelFactory.getKomgaEvents(),
                    LocalKeyEvents provides keyEvents,
                ) {
                    Navigator(LoginScreen())
                    AppNotifications(actualViewModelFactory.getAppNotifications())
                }
            } else LoadingMaxSizeIndicator()

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
