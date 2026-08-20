package snd.komelia.kavita

import kotlinx.browser.document
import kotlinx.browser.window
import kotlinx.coroutines.flow.MutableStateFlow
import org.w3c.dom.HTMLDivElement
import org.w3c.dom.HTMLElement
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
        if (parent.contains(element)) return true

        val editSeriesButton = parent.getElementsByTagName("button").asList().find {
            it.getAttribute("id") == "edit-btn--komf"
        }
        if (editSeriesButton != null) {
            editSeriesButton.parentElement?.insertAdjacentElement("afterend", element)
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
        return (document.querySelector("app-series-detail .info-container div h4 span") as HTMLElement).innerText
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