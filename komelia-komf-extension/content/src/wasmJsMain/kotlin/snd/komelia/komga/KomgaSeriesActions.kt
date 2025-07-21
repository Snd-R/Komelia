package snd.komelia.komga

import io.github.snd_r.komelia.ui.common.AppTheme
import kotlinx.browser.document
import kotlinx.browser.window
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import org.w3c.dom.HTMLButtonElement
import org.w3c.dom.HTMLElement
import org.w3c.dom.asList
import snd.komelia.KomfActiveDialog
import snd.komf.api.KomfServerLibraryId
import snd.komf.api.KomfServerSeriesId

class KomgaSeriesActions(
    private val theme: StateFlow<AppTheme>,
    private val currentDialog: MutableStateFlow<KomfActiveDialog>,
) {
    val element: HTMLButtonElement
    private val dropdown = KomgaDropdown(
        listOf(
            KomgaDropdown.DropdownItem("Identify", this::onIdentifyClick),
            KomgaDropdown.DropdownItem("Reset Metadata", this::onResetMetadataClick),
        )
    )

    init {

        element = document.createElement("button") as HTMLButtonElement
        element.type = "button"
        element.classList.value = "v-btn v-btn--icon v-btn--round theme--dark v-size--default"
        element.innerHTML =
            "<span class=\"v-btn__content\"><i aria-hidden=\"true\" class=\"v-icon notranslate mdi mdi-puzzle theme--dark\"></i></span>"
        element.addEventListener("focus") { event -> (event.target as HTMLElement).blur() }

        element.addEventListener("click") { event ->
            val rect = element.getBoundingClientRect()
            if (dropdown.isShown) {
                dropdown.hide()
            } else {
                dropdown.show(
                    rect.bottom.toInt(),
                    rect.left.toInt()
                )
            }
        }
        document.addEventListener("click") { event ->
            val target = event.target
            if (target is HTMLElement
                && !dropdown.element.contains(target)
                && !element.contains(target) && element != target
            ) {
                dropdown.hide()
            }
        }

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


    fun onMount() {
        if (theme.value == AppTheme.LIGHT) {
            (element.getElementsByClassName("theme--dark").asList().toList() + element
                    + dropdown.element.getElementsByClassName("theme--dark").asList().toList())
                .forEach { it.classList.replace("theme--dark", "theme--light") }
        }

        document.getElementById("app")?.appendChild(dropdown.element)
    }

    fun getSeriesTitle(): String? {
        val seriesTitle = document.querySelector(
            ".v-main__wrap .v-toolbar__content .v-toolbar__title span"
        ) as? HTMLElement
        if (seriesTitle != null) return seriesTitle.innerText

        val oneshotTitle = document.querySelector(
            ".v-main__wrap .container--fluid .container span.text-h6"
        ) as? HTMLElement
        if (oneshotTitle != null) return oneshotTitle.innerText

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
        val toolbar = document.querySelector(".v-main__wrap .v-toolbar__content") ?: return null
        val libraryId = toolbar.children.asList().find {
            val href = it.getAttribute("href") ?: return@find false
            href.contains("libraries")
        }?.getAttribute("href")?.split("/")?.getOrNull(2)

        return libraryId?.let { KomfServerLibraryId(it) }
    }
}