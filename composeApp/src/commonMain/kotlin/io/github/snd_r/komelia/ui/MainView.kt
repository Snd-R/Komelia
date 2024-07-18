package io.github.snd_r.komelia.ui

import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.key.KeyEvent
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.unit.dp
import cafe.adriel.lyricist.ProvideStrings
import cafe.adriel.voyager.navigator.Navigator
import com.dokar.sonner.ToastWidthPolicy
import com.dokar.sonner.Toaster
import com.dokar.sonner.ToasterState
import com.dokar.sonner.listenMany
import com.dokar.sonner.rememberToasterState
import io.github.snd_r.komelia.AppNotifications
import io.github.snd_r.komelia.DependencyContainer
import io.github.snd_r.komelia.ViewModelFactory
import io.github.snd_r.komelia.platform.BackPressHandler
import io.github.snd_r.komelia.platform.ConfigurePlatformTheme
import io.github.snd_r.komelia.platform.PlatformTitleBar
import io.github.snd_r.komelia.platform.PlatformType
import io.github.snd_r.komelia.platform.WindowWidth
import io.github.snd_r.komelia.strings.EnStrings
import io.github.snd_r.komelia.toToast
import io.github.snd_r.komelia.ui.common.AppTheme
import io.github.snd_r.komelia.ui.common.LoadingMaxSizeIndicator
import io.github.snd_r.komelia.ui.dialogs.update.UpdateDialog
import io.github.snd_r.komelia.ui.dialogs.update.UpdateProgressDialog
import io.github.snd_r.komelia.ui.login.LoginScreen
import io.github.snd_r.komelia.updates.AppRelease
import io.github.snd_r.komelia.updates.StartupUpdateChecker
import io.github.snd_r.komga.sse.KomgaEvent
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

val LocalViewModelFactory = compositionLocalOf<ViewModelFactory> { error("ViewModel factory is not set") }
val LocalToaster = compositionLocalOf<ToasterState> { error("Toaster is not set") }
val LocalKomgaEvents = compositionLocalOf<SharedFlow<KomgaEvent>> { error("Komga events are not set") }
val LocalKeyEvents = compositionLocalOf<SharedFlow<KeyEvent>> { error("Key events are not set") }
val LocalWindowWidth = compositionLocalOf<WindowWidth> { error("Window size is not set") }
val LocalStrings = staticCompositionLocalOf { EnStrings }
val LocalPlatform = compositionLocalOf<PlatformType> { error("Platform type is not set") }

private val vmFactory = MutableStateFlow<ViewModelFactory?>(null)

@Composable
fun MainView(
    dependencies: DependencyContainer?,
    windowWidth: WindowWidth,
    platformType: PlatformType,
    keyEvents: SharedFlow<KeyEvent>
) {
    val theme = dependencies?.settingsRepository?.getAppTheme()
        ?.collectAsState(AppTheme.DARK)?.value
        ?: AppTheme.DARK

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
                    vmFactory.value = ViewModelFactory(dependencies)
                }
            }

            if (viewModelFactory == null) return@Surface

            ProvideStrings(dependencies.lyricist, LocalStrings) {
                val notificationToaster = rememberToasterState()

                CompositionLocalProvider(
                    LocalViewModelFactory provides viewModelFactory,
                    LocalToaster provides notificationToaster,
                    LocalKomgaEvents provides viewModelFactory.getKomgaEvents(),
                    LocalKeyEvents provides keyEvents,
                    LocalWindowWidth provides windowWidth,
                    LocalPlatform provides platformType
                ) {

                    Navigator(
                        screen = LoginScreen(),
                        onBackPressed = null
                    )

                    AppNotifications(viewModelFactory.appNotifications)

                    val updateChecker = remember { viewModelFactory.getStartupUpdateChecker() }

                    StartupUpdateChecker(updateChecker)


                }
            }

            BackPressHandler {}
        }
    }
}


@Composable
fun AppNotifications(
    appNotifications: AppNotifications,
) {
    val toaster = rememberToasterState(onToastDismissed = { appNotifications.remove(it.id as Long) })

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


@Composable
private fun StartupUpdateChecker(updater: StartupUpdateChecker) {
    val coroutineScope = rememberCoroutineScope()
    var newRelease by remember { mutableStateOf<AppRelease?>(null) }
    LaunchedEffect(Unit) { updater.checkForUpdates()?.let { newRelease = it } }

    val progress = updater.downloadProgress.collectAsState().value
    val release = newRelease
    if (release != null) {
        UpdateDialog(
            newRelease = release,
            onConfirm = {
                coroutineScope.launch {
                    updater.onUpdate(release)
                    newRelease = null
                }
            },
            onDismiss = {
                coroutineScope.launch { updater.onUpdateDismiss(release) }
                newRelease = null
            }
        )
    }
    if (progress != null) {
        UpdateProgressDialog(
            totalSize = progress.total,
            downloadedSize = progress.completed,
            onCancel = updater::onUpdateCancel
        )
    }
}