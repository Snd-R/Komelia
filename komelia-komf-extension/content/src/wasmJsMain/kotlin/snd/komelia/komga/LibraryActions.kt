package snd.komelia.komga

import io.github.snd_r.komelia.ui.common.AppTheme
import kotlinx.browser.document
import kotlinx.coroutines.flow.StateFlow
import org.w3c.dom.HTMLButtonElement
import org.w3c.dom.HTMLElement
import org.w3c.dom.asList
import snd.komelia.logger
import snd.komf.api.KomfServerLibraryId
import kotlin.collections.forEach
import kotlin.collections.plus

class LibraryActions(
    private val theme: StateFlow<AppTheme>,
    onIdentifyClick: () -> Unit,
    onResetClick: () -> Unit,
) {
    val element: HTMLButtonElement
    private val dropdown = KomgaDropdown(
        listOf(
            KomgaDropdown.DropdownItem("Auto-Identify", onIdentifyClick),
            KomgaDropdown.DropdownItem("Reset Metadata", onResetClick),
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

    fun onMount() {
        logger.info { "libraryActions on mount" }
        if (theme.value == AppTheme.LIGHT) {
            (element.getElementsByClassName("theme--dark").asList().toList() + element
                    + dropdown.element.getElementsByClassName("theme--dark").asList().toList() )
                .forEach { it.classList.replace("theme--dark", "theme--light") }
        }
        document.getElementById("app")?.appendChild(dropdown.element)
    }

    fun getLibraryId(): KomfServerLibraryId? {
        val urlPath = document.location?.pathname?.split("/") ?: return null
        val idx = urlPath.indexOfFirst { it == "libraries" }
        val libraryId = urlPath.getOrNull(idx + 1)?.let { KomfServerLibraryId(it) }
        logger.info { "detected libraryId ${libraryId?.value}" }
        return libraryId
    }
}