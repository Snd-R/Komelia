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
import io.github.snd_r.komelia.ui.*
import io.github.snd_r.komelia.ui.common.AppTheme
import kotlinx.browser.document
import kotlinx.browser.localStorage
import kotlinx.browser.window
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import org.w3c.dom.*
import snd.komelia.komga.LibraryActions
import snd.komelia.komga.SeriesActions
import snd.komelia.settings.KomfSettingsDialog

class AppState(
    private val viewModelFactory: KomfViewModelFactory,
) {
    private val coroutineScope = CoroutineScope(Dispatchers.Default + SupervisorJob())
    private val observer: MutationObserver

    private val keyEvents = MutableSharedFlow<KeyEvent>()
    private val windowWidth = MutableStateFlow(WindowSizeClass.fromDp(window.innerWidth.dp))
    private val windowHeight = MutableStateFlow(WindowSizeClass.fromDp(window.innerHeight.dp))

    private val mounted = MutableStateFlow(false)
    private val currentDialog = MutableStateFlow(KomfDialog.NONE)
    private val theme = MutableStateFlow(AppTheme.DARK)

    private val komfErrorCanvas: HTMLCanvasElement
    private val komfDialogCanvas: HTMLCanvasElement
    private val komfDialog: HTMLDialogElement
    private val settingsButton: HTMLDivElement
    private val seriesActions = SeriesActions(
        theme = theme,
        onIdentifyClick = {
            currentDialog.value = KomfDialog.SERIES_IDENTIFY
            komfDialog.showModal()
        },
        onResetClick = {
            currentDialog.value = KomfDialog.SERIES_RESET
            komfDialog.showModal()
        },
    )
    private val libraryActions = LibraryActions(
        theme = theme,
        onIdentifyClick = {
            currentDialog.value = KomfDialog.LIBRARY_IDENTIFY
            komfDialog.showModal()
        },
        onResetClick = {
            currentDialog.value = KomfDialog.LIBRARY_RESET
            komfDialog.showModal()
        },
    )

    init {
        komfErrorCanvas = (document.createElement("canvas") as HTMLCanvasElement)
        komfErrorCanvas.id = "komf-error-canvas"
        komfErrorCanvas.style.position = "absolute"
        komfErrorCanvas.style.top = "0"
        komfErrorCanvas.style.left = "0"
        komfErrorCanvas.style.zIndex = "10000"
        komfErrorCanvas.style.setProperty("pointer-events", "none")

        komfDialogCanvas = (document.createElement("canvas") as HTMLCanvasElement)
        komfDialogCanvas.id = "komf-canvas"
        komfDialogCanvas.style.position = "absolute"
        komfDialogCanvas.style.zIndex = "10000"

        komfDialog = document.createElement("dialog") as HTMLDialogElement
        komfDialog.appendChild(komfDialogCanvas)
        komfDialog.style.width = "100%"
        komfDialog.style.height = "100%"
        komfDialog.style.maxWidth = "100vw"
        komfDialog.style.maxHeight = "100vh"
        komfDialog.style.padding = "0"
        komfDialog.style.border = "0"
        komfDialog.style.background = "transparent"
        komfDialog.style.position = "absolute"

        settingsButton = document.createElement("div") as HTMLDivElement
        settingsButton.className = "v-list-group v-list-group--no-action"
        settingsButton.innerHTML = """
<div tabindex="0" aria-expanded="false" role="button" class="v-list-group__header v-list-item v-list-item--link theme--dark">
   <div class="v-list-item__icon v-list-group__header__prepend-icon"><i aria-hidden="true" class="v-icon notranslate mdi mdi-puzzle theme--dark"></i></div>
   <div class="v-list-item__title">Komf settings</div>
</div>
"""
        settingsButton.addEventListener("click") { event ->
            currentDialog.value = KomfDialog.SETTINGS
            komfDialog.showModal()
        }
        (settingsButton.children[0] as HTMLElement).addEventListener("focus") { event ->
            (event.target as HTMLElement).blur()
        }

        observer = MutationObserver { mutations, observer ->
            mutations.toList().forEach { mutation ->
                if (mutation.removedNodes.length == 0 && mutation.addedNodes.length == 0) return@forEach
                checkMutation(mutation)
            }
        }
    }

    fun launch() {
        observer.observe(document, mutationObserverConfig())
        window.addEventListener("resize") {
            windowWidth.value = WindowSizeClass.fromDp(window.innerWidth.dp)
            windowHeight.value = WindowSizeClass.fromDp(window.innerHeight.dp)
        }

        localStorage.getItem("vuex")?.let {
            val json = Json.decodeFromString<JsonObject>(it)
            val persistedState = json["persistedState"] as? JsonObject
            val komgaTheme = persistedState?.get("theme") as? JsonPrimitive
            if (komgaTheme != null) {
                when (komgaTheme.content) {
                    "theme.dark" -> this.theme.value = AppTheme.DARK
                    "theme.system" -> {
                        if (window.matchMedia("(prefers-color-scheme: dark)").matches)
                            this.theme.value = AppTheme.DARK
                        else this.theme.value = AppTheme.LIGHT

                    }

                    else -> this.theme.value = AppTheme.LIGHT
                }
            }
        }

        if (theme.value == AppTheme.LIGHT) {
            settingsButton.getElementsByClassName("theme--dark").asList().toList().forEach { elem ->
                elem.classList.replace("theme--dark", "theme--light")
            }
        }

        tryMount()

        mounted.onEach { mounted ->
            if (!mounted) return@onEach
            document.body?.appendChild(komfDialog)
            document.body?.appendChild(komfErrorCanvas)
            seriesActions.onMount()
            libraryActions.onMount()

            startDialogContentApp()
            startErrorNotificationsApp()
            logger.info { "started compose app" }
        }.launchIn(coroutineScope)

        window.addEventListener(
            type = "scroll",
            callback = { event -> if (currentDialog.value != KomfDialog.NONE) event.preventDefault() },
            options = AddEventListenerOptions(passive = false)
        )
        window.addEventListener(
            type = "wheel",
            callback = { event -> if (currentDialog.value != KomfDialog.NONE) event.preventDefault() },
            options = AddEventListenerOptions(passive = false)
        )

    }

    private fun tryMount() {

        if (document.body != null) {
            logger.info { "document already has body. Checking if mount point is present" }
            document.body?.let { tryMountHtmlElements(it) }
            if (mounted.value) return
            else logger.info { "could not find mount point" }
        }
        coroutineScope.launch {
            try {
                logger.info { "awaiting mount point result from mutation observer; 500ms timeout" }
                withTimeout(500) { mounted.first { it } }
            } catch (_: TimeoutCancellationException) {
                logger.info { "mount await timeout, polling document body" }
                if (document.body == null) {
                    logger.info { "document body is null; waiting 500ms until retry" }
                    delay(500)
                }
                if (!mounted.value)
                    document.body?.let { tryMountHtmlElements(it) } ?: error("document body is null")

                if (!mounted.value) error("failed to find mount point")
            }
        }
    }

    private fun checkMutation(mutation: MutationRecord) {
        if (mutation.removedNodes.length == 0 && mutation.addedNodes.length == 0) return

        mutation.addedNodes.asList().forEach { node ->
            if (node.nodeName != "DIV" || node.childNodes.length == 0) {
                return@forEach
            }
            tryMountHtmlElements(node as HTMLElement)
        }
    }


    private fun tryMountHtmlElements(parentElement: HTMLElement) {
        val drawer_content = parentElement.getElementsByClassName("v-navigation-drawer__content").asList()
        val menus = drawer_content
            .find { drawerNode -> drawerNode.parentElement?.tagName == "NAV" }
            ?.children?.item(2)

        if (menus != null) {
            logger.info { "detected settings button mount point" }
            menus.insertBefore(settingsButton, menus.children.asList().last())
            mounted.value = true
        }
        val toolbar = parentElement.querySelector(".v-main__wrap .v-toolbar__content")
        val toolbarParent = toolbar?.parentElement
        if (toolbar != null && toolbarParent != null && !toolbarParent.classList.contains("hidden-sm-and-up")) {
            val path = window.location.pathname.split("/").reversed()
            logger.info { "detecting current screen from url; current path: $path" }
            if (path.any { it == "libraries" }) {
                logger.info { "detected library screen; mounting library actions" }
                toolbar.children[4]?.insertAdjacentElement("afterend", libraryActions.element)
            } else if (path.any { it == "series" }) {
                logger.info { "detected series screen; mounting series actions" }
                toolbar.children[4]?.insertAdjacentElement("afterend", seriesActions.element)
            } else if (path.any { it == "oneshot" }) {
                logger.info { "detected oneshot screen; mounting series actions" }
                toolbar.children.asList()
                    .find { it.tagName == "BUTTON" }
                    ?.insertAdjacentElement("afterend", seriesActions.element)
            }
        }
    }

    @OptIn(ExperimentalComposeUiApi::class)
    private fun startDialogContentApp() {
        CanvasBasedWindow(canvasElementId = komfDialogCanvas.id) {
            var theme = this.theme.collectAsState().value
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
                        val dismissRequest = {
                            this@AppState.currentDialog.value = KomfDialog.NONE
                            komfDialog.close()
                        }

                        when (currentDialog) {
                            KomfDialog.SETTINGS -> KomfSettingsDialog(onDismiss = dismissRequest)

                            KomfDialog.SERIES_IDENTIFY -> IdentifyDialog(
                                seriesActions.getSeriesId(),
                                seriesActions.getLibraryId(),
                                seriesName = seriesActions.getSeriesTitle(),
                                onDismissRequest = dismissRequest
                            )

                            KomfDialog.SERIES_RESET -> ResetSeriesMetadataDialog(
                                seriesId = seriesActions.getSeriesId(),
                                libraryId = seriesActions.getLibraryId(),
                                onDismissRequest = dismissRequest
                            )

                            KomfDialog.LIBRARY_RESET -> ResetLibraryMetadataDialog(
                                libraryId = libraryActions.getLibraryId(),
                                onDismissRequest = dismissRequest
                            )

                            KomfDialog.LIBRARY_IDENTIFY -> LibraryAutoIdentifyDialog(
                                libraryId = libraryActions.getLibraryId(),
                                onDismissRequest = dismissRequest
                            )

                            KomfDialog.NONE -> {}
                        }
                    }
                }
            }
        }
    }

    @OptIn(ExperimentalComposeUiApi::class)
    private fun startErrorNotificationsApp() {
        CanvasBasedWindow(canvasElementId = komfErrorCanvas.id) {
            var theme = this.theme.collectAsState().value
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

enum class KomfDialog {
    SETTINGS,
    SERIES_IDENTIFY,
    SERIES_RESET,
    LIBRARY_RESET,
    LIBRARY_IDENTIFY,
    NONE
}
