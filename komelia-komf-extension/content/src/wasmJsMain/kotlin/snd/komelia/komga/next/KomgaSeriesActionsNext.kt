package snd.komelia.komga.next

import kotlinx.browser.document
import kotlinx.browser.window
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import org.w3c.dom.HTMLAnchorElement
import org.w3c.dom.HTMLButtonElement
import org.w3c.dom.HTMLDivElement
import org.w3c.dom.HTMLElement
import org.w3c.dom.asList
import snd.komelia.KomfActiveDialog
import snd.komelia.komga.changeTheme
import snd.komelia.ui.Theme
import snd.komf.api.KomfServerLibraryId
import snd.komf.api.KomfServerSeriesId

class KomgaSeriesActionsNext(
    theme: StateFlow<Theme>,
    private val currentDialog: MutableStateFlow<KomfActiveDialog>,
) {
    private val coroutineScope = CoroutineScope(Dispatchers.Default)
    private val element: HTMLButtonElement = document.createElement("button") as HTMLButtonElement
    private val dropdown = KomgaDropdownNext(
        parent = element,
        items = listOf(
            KomgaDropdownNext.DropdownItem("Identify", this::onIdentifyClick),
            KomgaDropdownNext.DropdownItem("Reset Metadata", this::onResetMetadataClick),
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

        val seriesEditButton =
            parent.querySelectorAll("[action=\"EDIT_SERIES\"]").asList().firstOrNull() as? HTMLButtonElement

        if (seriesEditButton != null) {
            seriesEditButton.insertAdjacentElement("afterend", element)
            dropdown.tryMount()
            return true
        }

        return false
    }

    private fun onIdentifyClick() {
        val seriesId = getSeriesId()
        val libraryId = getLibraryId()
        val seriesTitle = getSeriesTitle()
        when {
            seriesId == null -> currentDialog.value = KomfActiveDialog.ErrorDialog("Failed to fine seriesId")
            libraryId == null -> currentDialog.value = KomfActiveDialog.ErrorDialog("Failed to find libraryId")
            seriesTitle == null -> currentDialog.value = KomfActiveDialog.ErrorDialog("Failed to find series title")
            else -> currentDialog.value = KomfActiveDialog.SeriesIdentify(
                seriesId = seriesId,
                libraryId = libraryId,
                seriesTitle = seriesTitle
            )
        }
    }

    private fun onResetMetadataClick() {
        val seriesId = getSeriesId()
        val libraryId = getLibraryId()
        when {
            seriesId == null -> currentDialog.value = KomfActiveDialog.ErrorDialog("Failed to fine seriesId")
            libraryId == null -> currentDialog.value = KomfActiveDialog.ErrorDialog("Failed to find libraryId")
            else -> currentDialog.value = KomfActiveDialog.SeriesReset(
                seriesId = seriesId,
                libraryId = libraryId,
            )
        }

    }

    fun getSeriesTitle(): String? {
        val seriesTitle = document.querySelector(".text-headline-small") as? HTMLDivElement
        if (seriesTitle != null) return seriesTitle.innerText

        return null
    }

    fun getSeriesId(): KomfServerSeriesId? {
        val urlPath = window.location.pathname.split("/")
        val idx = urlPath.indexOfFirst { it == "series" || it == "oneshot" }
        if (idx == -1) return null
        val seriesId = urlPath.getOrNull(idx + 1)?.let { KomfServerSeriesId(it) }
        return seriesId
    }


    fun getLibraryId(): KomfServerLibraryId? {
        val toolbar = document.querySelectorAll(".v-toolbar__content").asList()
            .firstOrNull { it.parentNode?.parentNode?.parentNode?.nodeName == "MAIN" } as? HTMLDivElement
            ?: return null

        val libraryAnchor = toolbar.firstElementChild?.firstElementChild?.firstElementChild as? HTMLAnchorElement
            ?: return null

        val libraryPath = libraryAnchor.href.split("/")
        val libraryPathIdx = libraryPath.indexOf("libraries")
        if (libraryPathIdx == -1) return null

        return libraryPath.getOrNull(libraryPathIdx + 1)?.let { KomfServerLibraryId(it) }
    }
}