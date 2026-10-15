package snd.komelia.kavita

import kotlinx.browser.document
import kotlinx.browser.window
import kotlinx.coroutines.flow.MutableStateFlow
import org.w3c.dom.HTMLDivElement
import org.w3c.dom.HTMLElement
import org.w3c.dom.Node
import org.w3c.dom.asList
import snd.komelia.KomfActiveDialog
import snd.komf.api.KomfServerLibraryId
import snd.komf.api.KomfServerSeriesId

class KavitaSeriesActions(
    private val currentDialog: MutableStateFlow<KomfActiveDialog>,
) {
    val element: HTMLDivElement = document.createElement("div") as HTMLDivElement
    private val dropdown = KavitaDropdown(
        parent = element,
        items = listOf(
            KavitaDropdown.DropdownItem("Identify", this::onIdentifyClick),
            KavitaDropdown.DropdownItem("Reset Metadata", this::onResetMetadataClick),
        )
    )

    init {
        element.classList.value = "col-auto ms-2"
        element.innerHTML =
            """<button title="Komf Identify" class="btn btn-actions"><span><i aria-hidden="true" class="fa fa-pen-to-square"></i></span></button>"""
        element.addEventListener("focus") { event -> (event.target as HTMLElement).blur() }
    }

    fun tryMount(parent: HTMLElement): Boolean {
        // Kavita only renders the edit button for admins on the series detail page
        val editButtonContainer = parent.querySelector("#edit-btn--komf")?.parentElement
        if (editButtonContainer == null) {
            element.remove()
            dropdown.hide()
            return false
        }

        if (editButtonContainer.nextElementSibling != element) {
            editButtonContainer.insertAdjacentElement("afterend", element)
            dropdown.hide()
        }
        dropdown.tryMount()
        return true
    }

    private fun onIdentifyClick() {
        val seriesId = getSeriesId()
        val libraryId = getLibraryId()
        val seriesTitle = getSeriesTitle()
        when {
            seriesId == null -> currentDialog.value = KomfActiveDialog.ErrorDialog("Failed to find seriesId")
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
            seriesId == null -> currentDialog.value = KomfActiveDialog.ErrorDialog("Failed to find seriesId")
            libraryId == null -> currentDialog.value = KomfActiveDialog.ErrorDialog("Failed to find libraryId")
            else -> currentDialog.value = KomfActiveDialog.SeriesReset(
                seriesId = seriesId,
                libraryId = libraryId,
            )
        }

    }

    fun getSeriesTitle(): String? {
        val titleElement = document.querySelector("app-series-detail .info-container h4 span") ?: return null
        // only take direct text, title span can also contain a loading spinner with hidden text
        val title = titleElement.childNodes.asList()
            .filter { it.nodeType == Node.TEXT_NODE }
            .joinToString("") { it.textContent ?: "" }
            .trim()
            .ifBlank { (titleElement as? HTMLElement)?.innerText?.trim() }
        return title?.ifBlank { null }
    }

    fun getSeriesId(): KomfServerSeriesId? {
        val urlPath = window.location.pathname.split("/")
        val idx = urlPath.indexOfFirst { it == "series" }
        if (idx == -1) return null
        val seriesId = urlPath.getOrNull(idx + 1)
        return seriesId?.let { KomfServerSeriesId(it) }
    }


    fun getLibraryId(): KomfServerLibraryId? {
        val urlPath = window.location.pathname.split('/')
        val idx = urlPath.indexOfFirst { it == "library" }
        if (idx == -1) return null
        val libraryId = urlPath.getOrNull(idx + 1)
        return libraryId?.let { KomfServerLibraryId(it) }
    }
}