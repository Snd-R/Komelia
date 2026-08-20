package snd.komelia.kavita

import kotlinx.browser.document
import kotlinx.browser.window
import kotlinx.coroutines.flow.MutableStateFlow
import org.w3c.dom.HTMLButtonElement
import org.w3c.dom.HTMLElement
import org.w3c.dom.asList
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
        if (libraryId == null) currentDialog.value = KomfActiveDialog.ErrorDialog("Failed to fine libraryId")
        else currentDialog.value = KomfActiveDialog.LibraryIdentify(libraryId)
    }

    private fun onResetClick() {
        val libraryId = getLibraryId()
        if (libraryId == null) currentDialog.value = KomfActiveDialog.ErrorDialog("Failed to fine libraryId")
        else currentDialog.value = KomfActiveDialog.LibraryReset(libraryId)
    }

    fun tryMount(parent: HTMLElement): Boolean {
        if (parent.contains(element)) return true
        val libraryFilterButton = parent.getElementsByTagName("button").asList().find {
            it.getAttribute("id") == "filter-btn--komf"
        }
        if (libraryFilterButton != null) {
            libraryFilterButton.parentElement?.append(element)
            dropdown.tryMount()
            return true
        }

        return false
    }

    fun getLibraryId(): KomfServerLibraryId? {
        val urlPath = window.location.pathname.split('/')
        val idx = urlPath.indexOfFirst { it == "library" }
        if (idx == -1) return null
        val libraryId = urlPath.getOrNull(idx + 1)
        return libraryId?.let { KomfServerLibraryId(it) }
    }
}