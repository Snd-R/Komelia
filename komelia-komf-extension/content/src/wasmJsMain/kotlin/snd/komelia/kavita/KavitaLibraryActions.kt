package snd.komelia.kavita

import kotlinx.browser.document
import kotlinx.browser.window
import kotlinx.coroutines.flow.MutableStateFlow
import org.w3c.dom.HTMLButtonElement
import org.w3c.dom.HTMLElement
import snd.komelia.KomfActiveDialog
import snd.komf.api.KomfServerLibraryId

class KavitaLibraryActions(
    private val currentDialog: MutableStateFlow<KomfActiveDialog>,
) {
    val element: HTMLButtonElement = document.createElement("button") as HTMLButtonElement
    private val dropdown = KavitaDropdown(
        parent = element,
        items = listOf(
            KavitaDropdown.DropdownItem("Auto-Identify", this::onIdentifyClick),
            KavitaDropdown.DropdownItem("Reset Metadata", this::onResetClick),
        )
    )

    init {
        element.title = "Komf Identify"
        element.classList.value = "btn btn-icon btn-small"
        element.innerHTML = """<i aria-hidden="true" class="fa fa-pen-to-square"></i>"""
        element.addEventListener("focus") { event -> (event.target as HTMLElement).blur() }
    }

    private fun onIdentifyClick() {
        val libraryId = getLibraryId()
        if (libraryId == null) currentDialog.value = KomfActiveDialog.ErrorDialog("Failed to find libraryId")
        else currentDialog.value = KomfActiveDialog.LibraryIdentify(libraryId)
    }

    private fun onResetClick() {
        val libraryId = getLibraryId()
        if (libraryId == null) currentDialog.value = KomfActiveDialog.ErrorDialog("Failed to find libraryId")
        else currentDialog.value = KomfActiveDialog.LibraryReset(libraryId)
    }

    fun tryMount(parent: HTMLElement): Boolean {
        // filter button is also shown on pages that are not tied to a library (all series, want to read etc.)
        val filterButtonContainer = if (getLibraryId() != null && !isSeriesPage()) {
            parent.querySelector("#filter-btn--komf")?.parentElement
        } else null

        if (filterButtonContainer == null) {
            element.remove()
            dropdown.hide()
            return false
        }

        if (element.parentElement != filterButtonContainer || filterButtonContainer.lastElementChild != element) {
            filterButtonContainer.append(element)
            dropdown.hide()
        }
        dropdown.tryMount()
        return true
    }

    private fun isSeriesPage() = window.location.pathname.split('/').any { it == "series" }

    fun getLibraryId(): KomfServerLibraryId? {
        val urlPath = window.location.pathname.split('/')
        val idx = urlPath.indexOfFirst { it == "library" }
        if (idx == -1) return null
        val libraryId = urlPath.getOrNull(idx + 1)
        return libraryId?.let { KomfServerLibraryId(it) }
    }
}