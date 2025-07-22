package snd.komelia

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.key.KeyEvent
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.CanvasBasedWindow
import io.github.snd_r.komelia.platform.PlatformType
import io.github.snd_r.komelia.platform.WindowSizeClass
import io.github.snd_r.komelia.ui.AppNotifications
import io.github.snd_r.komelia.ui.LocalKeyEvents
import io.github.snd_r.komelia.ui.LocalPlatform
import io.github.snd_r.komelia.ui.LocalTheme
import io.github.snd_r.komelia.ui.LocalWindowHeight
import io.github.snd_r.komelia.ui.LocalWindowWidth
import io.github.snd_r.komelia.ui.common.AppTheme
import kotlinx.browser.document
import kotlinx.browser.window
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import org.w3c.dom.AddEventListenerOptions
import org.w3c.dom.HTMLCanvasElement
import org.w3c.dom.HTMLDialogElement
import org.w3c.dom.HTMLElement
import org.w3c.dom.MutationObserver
import org.w3c.dom.MutationObserverInit
import org.w3c.dom.Node
import org.w3c.dom.asList
import snd.komelia.dialogs.ErrorDialog
import snd.komelia.dialogs.IdentifyDialog
import snd.komelia.dialogs.LibraryAutoIdentifyDialog
import snd.komelia.dialogs.ResetLibraryMetadataDialog
import snd.komelia.dialogs.ResetSeriesMetadataDialog
import snd.komelia.kavita.KavitaComponent
import snd.komelia.komga.KomgaComponent
import snd.komelia.dialogs.SettingsDialog
import snd.komf.api.KomfServerLibraryId
import snd.komf.api.KomfServerSeriesId
import snd.komf.api.MediaServer

class AppState(
    private val viewModelFactory: KomfViewModelFactory,
) {
    private val coroutineScope = CoroutineScope(Dispatchers.Default + SupervisorJob())
    private val keyEvents = MutableSharedFlow<KeyEvent>()

    private val komfErrorCanvas: HTMLCanvasElement = (document.createElement("canvas") as HTMLCanvasElement)
    private val komfDialogCanvas: HTMLCanvasElement = (document.createElement("canvas") as HTMLCanvasElement)
    private val komfDialog: HTMLDialogElement
    private val windowWidth = MutableStateFlow(WindowSizeClass.fromDp(window.innerWidth.dp))
    private val windowHeight = MutableStateFlow(WindowSizeClass.fromDp(window.innerHeight.dp))

    private val theme = MutableStateFlow(AppTheme.DARK)
    private val currentDialog = MutableStateFlow<KomfActiveDialog>(KomfActiveDialog.None)

    private val mediaServer = MutableStateFlow(MediaServer.KOMGA)
    private val mediaServerComponent: MediaServerComponent
    private val observer: MutationObserver

    var mountEvent = MutableSharedFlow<Unit>(1, 0, BufferOverflow.DROP_OLDEST)

    init {
        komfErrorCanvas.id = "komf-error-canvas"
        komfErrorCanvas.style.position = "fixed"
        komfErrorCanvas.style.width = "100vw"
        komfErrorCanvas.style.height = "100vh"
        komfErrorCanvas.style.maxWidth = "100vw"
        komfErrorCanvas.style.maxHeight = "100vh"
        komfErrorCanvas.style.top = "0"
        komfErrorCanvas.style.left = "0"
        komfErrorCanvas.style.zIndex = "10000"
        komfErrorCanvas.style.setProperty("pointer-events", "none")
        komfErrorCanvas.style.overflowX = "hidden"
        komfErrorCanvas.style.overflowY = "hidden"

        komfDialogCanvas.id = "komf-canvas"
        komfDialogCanvas.style.zIndex = "10000"

        komfDialog = document.createElement("dialog") as HTMLDialogElement
        komfDialog.appendChild(komfDialogCanvas)
        komfDialog.style.width = "100vw"
        komfDialog.style.height = "100vh"
        komfDialog.style.maxWidth = "100vw"
        komfDialog.style.maxHeight = "100vh"
        komfDialog.style.padding = "0"
        komfDialog.style.border = "0"
        komfDialog.style.background = "transparent"
        komfDialog.style.overflowX = "hidden"
        komfDialog.style.overflowY = "hidden"

        when (document.title.split(' ')[0]) {
            "Kavita" -> {
                mediaServer.value = MediaServer.KAVITA
                mediaServerComponent = KavitaComponent(theme, currentDialog)
            }

            else -> {
                mediaServer.value = MediaServer.KOMGA
                mediaServerComponent = KomgaComponent(theme, currentDialog)
            }
        }
        observer = MutationObserver { mutations, observer ->
            mutations.toList().forEach { mutation ->
                if (mutation.removedNodes.length == 0 && mutation.addedNodes.length == 0) return@forEach
                for (node in mutation.addedNodes.asList()) {
                    if (node.nodeType != Node.ELEMENT_NODE || node.childNodes.length == 0) continue
                    val mounted = mediaServerComponent.tryMount(node as HTMLElement)
                    if (mounted) mountEvent.tryEmit(Unit)
                }
            }
        }
    }

    fun launch() {
        mountEvent.onEach {
            val body = document.body
            if (body == null || body.contains(komfDialog) || body.contains(komfErrorCanvas)) return@onEach
            body.appendChild(komfDialog)
            body.appendChild(komfErrorCanvas)

            startDialogContentApp()
            startErrorNotificationsApp()
            logger.info { "Started Komf extension app" }
        }.launchIn(coroutineScope)

        currentDialog.onEach {
            if (it is KomfActiveDialog.None) komfDialog.close()
            else komfDialog.showModal()
        }.launchIn(coroutineScope)

        window.addEventListener("resize") {
            windowWidth.value = WindowSizeClass.fromDp(window.innerWidth.dp)
            windowHeight.value = WindowSizeClass.fromDp(window.innerHeight.dp)
        }

        window.addEventListener(
            type = "scroll",
            callback = { event -> if (currentDialog.value != KomfActiveDialog.None) event.preventDefault() },
            options = AddEventListenerOptions(passive = false)
        )
        window.addEventListener(
            type = "wheel",
            callback = { event -> if (currentDialog.value != KomfActiveDialog.None) event.preventDefault() },
            options = AddEventListenerOptions(passive = false)
        )

        observer.observe(document, mutationObserverConfig())
        val body = document.body
        if (body != null) {
            val mounted = mediaServerComponent.tryMount(body)
            if (mounted) mountEvent.tryEmit(Unit)
        }
    }

    @OptIn(ExperimentalComposeUiApi::class)
    private fun startDialogContentApp() {
        CanvasBasedWindow(canvasElementId = komfDialogCanvas.id) {
            val theme = this.theme.collectAsState().value
            Box(
                modifier = Modifier
                    .drawBehind {
                        drawRect(
                            color = Color.Transparent,
                            size = this.size,
                            blendMode = BlendMode.Clear
                        )
                    }.fillMaxSize()
            ) {
                MaterialTheme(colorScheme = theme.colorScheme) {

                    CompositionLocalProvider(
                        LocalKeyEvents provides keyEvents,
                        LocalPlatform provides PlatformType.WEB_KOMF,
                        LocalTheme provides theme,
                        LocalWindowWidth provides windowWidth.collectAsState().value,
                        LocalWindowHeight provides windowHeight.collectAsState().value,
                        LocalKomfViewModelFactory provides viewModelFactory
                    ) {
                        val currentDialog = currentDialog.collectAsState().value
                        val onDismissRequest = {
                            this@AppState.currentDialog.value = KomfActiveDialog.None
                        }
                        val mediaServer = mediaServer.collectAsState().value

                        when (currentDialog) {
                            KomfActiveDialog.None -> {}

                            is KomfActiveDialog.LibraryIdentify -> LibraryAutoIdentifyDialog(
                                mediaServer = mediaServer,
                                libraryId = currentDialog.libraryId,
                                onDismissRequest = onDismissRequest
                            )

                            is KomfActiveDialog.LibraryReset -> ResetLibraryMetadataDialog(
                                mediaServer = mediaServer,
                                libraryId = currentDialog.libraryId,
                                onDismissRequest = onDismissRequest
                            )

                            is KomfActiveDialog.SeriesIdentify -> IdentifyDialog(
                                mediaServer = mediaServer,
                                currentDialog.seriesId,
                                currentDialog.libraryId,
                                seriesName = currentDialog.seriesTitle,
                                onDismissRequest = onDismissRequest
                            )

                            is KomfActiveDialog.SeriesReset -> ResetSeriesMetadataDialog(
                                mediaServer = mediaServer,
                                seriesId = currentDialog.seriesId,
                                libraryId = currentDialog.libraryId,
                                onDismissRequest = onDismissRequest
                            )

                            KomfActiveDialog.Settings -> SettingsDialog(
                                mediaServer = mediaServer,
                                onDismiss = onDismissRequest
                            )

                            is KomfActiveDialog.ErrorDialog -> ErrorDialog(
                                currentDialog.message,
                                onDismissRequest = onDismissRequest
                            )
                        }
                    }
                }
            }
        }
    }

    @OptIn(ExperimentalComposeUiApi::class)
    private fun startErrorNotificationsApp() {
        CanvasBasedWindow(canvasElementId = komfErrorCanvas.id) {
            val theme = this.theme.collectAsState().value
            Box(
                modifier = Modifier
                    .drawBehind {
                        drawRect(
                            color = Color.Transparent,
                            size = this.size,
                            blendMode = BlendMode.Clear
                        )
                    }.fillMaxSize()
            ) {
                MaterialTheme(colorScheme = theme.colorScheme) {
                    AppNotifications(
                        appNotifications = viewModelFactory.appNotifications,
                        theme = theme,
                        showCloseButton = false
                    )
                }
            }
        }
    }
}

sealed interface KomfActiveDialog {
    object Settings : KomfActiveDialog
    data class SeriesIdentify(
        val seriesId: KomfServerSeriesId,
        val libraryId: KomfServerLibraryId,
        val seriesTitle: String
    ) : KomfActiveDialog

    data class SeriesReset(
        val seriesId: KomfServerSeriesId,
        val libraryId: KomfServerLibraryId,
    ) : KomfActiveDialog

    data class LibraryReset(
        val libraryId: KomfServerLibraryId
    ) : KomfActiveDialog

    data class LibraryIdentify(
        val libraryId: KomfServerLibraryId
    ) : KomfActiveDialog

    data class ErrorDialog(
        val message: String
    ) : KomfActiveDialog

    data object None : KomfActiveDialog
}

fun mutationObserverConfig(): MutationObserverInit {
    js("return { childList: true, subtree: true };")
}
