package snd.komelia.ui

import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.key.KeyEvent
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.navigator.CurrentScreen
import cafe.adriel.voyager.navigator.Navigator
import cafe.adriel.voyager.navigator.NavigatorDisposeBehavior
import com.dokar.sonner.ToastWidthPolicy
import com.dokar.sonner.Toaster
import com.dokar.sonner.listenMany
import com.dokar.sonner.rememberToasterState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import snd.komelia.AppNotifications
import snd.komelia.KomgaAuthenticationState
import snd.komelia.KomgaAuthenticationState.DataState.AuthenticationRequired
import snd.komelia.KomgaAuthenticationState.DataState.Loaded
import snd.komelia.ui.Theme.Companion.toTheme
import snd.komelia.ui.Theme.ThemeType
import snd.komelia.ui.common.components.LoadingMaxSizeIndicator
import snd.komelia.ui.dialogs.update.UpdateDialog
import snd.komelia.ui.dialogs.update.UpdateProgressDialog
import snd.komelia.ui.komf.KomfMainScreen
import snd.komelia.ui.login.LoginScreen
import snd.komelia.ui.platform.BackPressHandler
import snd.komelia.ui.platform.ConfigurePlatformTheme
import snd.komelia.ui.platform.PlatformTitleBar
import snd.komelia.ui.platform.PlatformType
import snd.komelia.ui.platform.PlatformType.DESKTOP
import snd.komelia.ui.platform.PlatformType.MOBILE
import snd.komelia.ui.platform.PlatformType.WEB_KOMF
import snd.komelia.ui.platform.WindowSizeClass
import snd.komelia.updates.AppRelease
import snd.komelia.updates.StartupUpdateChecker

private val vmFactory = MutableStateFlow<ViewModelFactory?>(null)

@Composable
fun MainView(
    dependencies: DependencyContainer?,
    windowWidth: WindowSizeClass,
    windowHeight: WindowSizeClass,
    platformType: PlatformType,
    keyEvents: SharedFlow<KeyEvent>
) {
    var theme by rememberSaveable { mutableStateOf(Theme.DARK) }
    LaunchedEffect(dependencies) {
        dependencies?.appRepositories?.settingsRepository?.getAppTheme()?.collect { theme = it.toTheme() }
    }

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
                LocalKomgaEvents provides dependencies.komgaEvents.events,
                LocalKomfIntegration provides dependencies.appRepositories.komfSettingsRepository.getKomfEnabled(),
                LocalKeyEvents provides keyEvents,
                LocalPlatform provides platformType,
                LocalTheme provides theme,
                LocalWindowState provides dependencies.windowState,
                LocalWindowWidth provides windowWidth,
                LocalWindowHeight provides windowHeight,
                LocalLibraries provides dependencies.komgaSharedState.libraries,
                LocalReloadEvents provides viewModelFactory.screenReloadEvents,
                LocalBookDownloadEvents provides dependencies.offlineDependencies.bookDownloadEvents,
                LocalOfflineMode provides dependencies.isOffline,
                LocalKomgaState provides dependencies.komgaSharedState
            ) {
                MainContent(platformType, dependencies.komgaSharedState)

                AppNotifications(dependencies.appNotifications, theme)
                val updateChecker = remember { viewModelFactory.getStartupUpdateChecker() }
                if (updateChecker != null) {
                    StartupUpdateChecker(updateChecker)
                }
            }

            BackPressHandler {}
        }
    }
}

@Composable
private fun MainContent(
    platformType: PlatformType,
    komgaSharedState: KomgaAuthenticationState
) {
    val loginScreen = remember(platformType) {
        when (platformType) {
            MOBILE, DESKTOP -> LoginScreen()
            WEB_KOMF -> KomfMainScreen()
        }
    }

    Navigator(
        screen = loginScreen,
        disposeBehavior = NavigatorDisposeBehavior(disposeNestedNavigators = false),
        onBackPressed = null
    ) { navigator ->
        var canProceed by remember { mutableStateOf(komgaSharedState.authenticationState.value == Loaded) }
        // FIXME this looks like a hack. Find a multiplatform way to handle this outside of composition?
        // variable to track if Android app was killed in background and later restored
        var wasInitializedBefore by rememberSaveable { mutableStateOf(false) }
        navigator.clearEvent()

        LaunchedEffect(Unit) {
            if (canProceed) return@LaunchedEffect

            // not really necessary since Voyager navigator doesn't dispose existing MainScreen when it's replaced with LoginScreen
            // when LoginScreen replaces itself back to MainScreen, it's restored to old state
            // not sure if it's intended, do proper initialization here to avoid loading LoginScreen
            if (wasInitializedBefore) {
                komgaSharedState.tryReloadState()
            }

            val currentState = komgaSharedState.authenticationState.value
            when (currentState) {
                AuthenticationRequired -> navigator.replaceAll(loginScreen)
                Loaded -> {}
            }
            canProceed = true

            komgaSharedState.authenticationState.collect {
                wasInitializedBefore = when (it) {
                    AuthenticationRequired -> false
                    Loaded -> true
                }
            }
        }

        if (canProceed) CurrentScreen()
    }

}


@Composable
fun AppNotifications(
    appNotifications: AppNotifications,
    theme: Theme,
    showCloseButton: Boolean = true,
) {
    val toaster =
        rememberToasterState(onToastDismissed = { appNotifications.remove(it.id as Long) })

    LaunchedEffect(toaster) {
        val toastsFlow = appNotifications.getNotifications()
            .map { notifications -> notifications.map { it.toToast() } }
        toaster.listenMany(toastsFlow)
    }

    Toaster(
        state = toaster,
        richColors = true,
        darkTheme = theme.type == ThemeType.DARK,
        showCloseButton = showCloseButton,
        widthPolicy = { ToastWidthPolicy(max = 500.dp) },
        actionSlot = { toast ->
            when (toast.action) {
                null -> {}
                else -> {}
            }
        },
        //FIXME: on Android API 35 popup is shown under nav bar due to enforced edge to edge mode.
        // WindowInsets doesn't seem to provide any values inside dialogs
        // add offset from main window as workaround, as a side effect, on lower apis it's drawn higher than usual
        // there's no simple way to enable edge to edge in compose dialogs on lower apis
        offset = IntOffset(0, -WindowInsets.navigationBars.getBottom(LocalDensity.current))
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