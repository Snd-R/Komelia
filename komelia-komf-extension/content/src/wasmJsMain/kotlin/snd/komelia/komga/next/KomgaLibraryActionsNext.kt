package snd.komelia.komga.next

import kotlinx.browser.document
import kotlinx.browser.window
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import org.w3c.dom.HTMLButtonElement
import org.w3c.dom.HTMLDivElement
import org.w3c.dom.HTMLElement
import org.w3c.dom.asList
import snd.komelia.KomfActiveDialog
import snd.komelia.komga.changeTheme
import snd.komelia.logger
import snd.komelia.ui.Theme
import snd.komf.api.KomfServerLibraryId

class KomgaLibraryActionsNext(
    theme: StateFlow<Theme>,
    private val currentDialog: MutableStateFlow<KomfActiveDialog>,
) {
    private val coroutineScope = CoroutineScope(Dispatchers.Default)
    private val element: HTMLButtonElement = document.createElement("button") as HTMLButtonElement
    private val dropdown = KomgaDropdownNext(
        parent = element,
        items = listOf(
            KomgaDropdownNext.DropdownItem("Auto-Identify", this::onIdentifyClick),
            KomgaDropdownNext.DropdownItem("Reset Metadata", this::onResetClick),
        ),
        theme = theme
    )

    init {
        element.type = "button"
        element.classList.value = "v-icon-btn v-icon-btn--default v-theme--dark v-icon-btn--variant-flat"
        element.setAttribute("tabindex", "0")
        element.setAttribute("title", "Komf")
        element.setAttribute("style", "--v-icon-btn-height: 40px; --v-icon-btn-width: 40px;")
        element.setAttribute("aria-describedby", "v-tooltip-v-0")
        element.setAttribute("aria-haspopup", "dialog")
        element.innerHTML =
            """
                <span class="v-icon-btn__overlay"></span>
                <span class="v-icon-btn__underlay"></span>
                <div class="v-icon-btn__content" data-no-activator="">
                    <i class="i-mdi:pencil mdi v-icon notranslate v-theme--dark" aria-hidden="true" style="font-size: 24px; height: 24px; width: 24px;"></i>
                </div>
            """.trimIndent()

        theme.onEach { element.changeTheme(it) }.launchIn(coroutineScope)
    }

    fun tryMount(parent: HTMLElement): Boolean {
        if (parent.contains(element)) return true

        val mount = parent.querySelectorAll(".v-toolbar__append").asList()
            .firstOrNull { it.parentNode?.parentNode?.parentNode?.parentNode?.nodeName == "MAIN" } as? HTMLDivElement

        if (mount != null) {
            val path = window.location.pathname.split("/")
            val libraries = path.indexOf("libraries")
            if (libraries == -1) return false

            val pinned = path.size > libraries + 1 && path[libraries + 1] == "pinned"
            if (pinned) {
                logger.info { "library actions pinned" }
            }
            if (!pinned) {
                logger.info { "mount library actions" }
                mount.appendChild(element)
                dropdown.tryMount()
                return true
            }
        }

        return false
    }

    private fun onIdentifyClick() {
        val libraryId = getLibraryId()
        if (libraryId == null) currentDialog.value = KomfActiveDialog.ErrorDialog("Failed to fine libraryId")
        else currentDialog.value = KomfActiveDialog.LibraryIdentify(libraryId)
    }

    private fun onResetClick() {
        val libraryId = getLibraryId()
        if (libraryId == null) currentDialog.value = KomfActiveDialog.ErrorDialog("Failed to fine libraryId")
        else currentDialog.value = KomfActiveDialog.LibraryReset(libraryId)
    }

    fun getLibraryId(): KomfServerLibraryId? {
        val urlPath = window.location.pathname.split("/")
        val idx = urlPath.indexOfFirst { it == "libraries" }
        if (idx == -1) return null
        val libraryId = urlPath.getOrNull(idx + 1)?.let { KomfServerLibraryId(it) }
        return libraryId
    }
}

private fun logJs(x: JsAny): Unit = js("{console.log(x);}")