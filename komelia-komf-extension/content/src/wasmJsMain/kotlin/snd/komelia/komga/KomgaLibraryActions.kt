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

class KomgaLibraryActions(
    private val theme: StateFlow<AppTheme>,
    private val currentDialog: MutableStateFlow<KomfActiveDialog>,
) {
    val element: HTMLButtonElement = document.createElement("button") as HTMLButtonElement
    private val dropdown = KomgaDropdown(
        listOf(
            KomgaDropdown.DropdownItem("Auto-Identify", this::onIdentifyClick),
            KomgaDropdown.DropdownItem("Reset Metadata", this::onResetClick),
        )
    )

    init {
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
        val libraryId = getLibraryId()
        if (libraryId == null) currentDialog.value = KomfActiveDialog.ErrorDialog("Failed to fine libraryId")
        else currentDialog.value = KomfActiveDialog.LibraryIdentify(libraryId)
    }

    private fun onResetClick() {
        val libraryId = getLibraryId()
        if (libraryId == null) currentDialog.value = KomfActiveDialog.ErrorDialog("Failed to fine libraryId")
        else currentDialog.value = KomfActiveDialog.LibraryReset(libraryId)
    }

    fun onMount() {
        if (theme.value == AppTheme.LIGHT) {
            (element.getElementsByClassName("theme--dark").asList().toList() + element
                    + dropdown.element.getElementsByClassName("theme--dark").asList().toList())
                .forEach { it.classList.replace("theme--dark", "theme--light") }
        }
        document.getElementById("app")?.appendChild(dropdown.element)
    }

    fun getLibraryId(): KomfServerLibraryId? {
        val urlPath = window.location.pathname.split("/")
        val idx = urlPath.indexOfFirst { it == "libraries" }
        if (idx == -1) return null
        val libraryId = urlPath.getOrNull(idx + 1)?.let { KomfServerLibraryId(it) }
        return libraryId
    }
}