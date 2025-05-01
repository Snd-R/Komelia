package snd.komelia.komga

import io.github.snd_r.komelia.ui.common.AppTheme
import kotlinx.browser.document
import kotlinx.coroutines.flow.StateFlow
import org.w3c.dom.HTMLButtonElement
import org.w3c.dom.HTMLElement
import org.w3c.dom.asList
import snd.komelia.logger
import snd.komf.api.KomfServerLibraryId
import snd.komf.api.KomfServerSeriesId
import kotlin.collections.plus

class SeriesActions(
    private val theme: StateFlow<AppTheme>,
    onIdentifyClick: () -> Unit,
    onResetClick: () -> Unit,
) {
    val element: HTMLButtonElement
    private val dropdown = KomgaDropdown(
        listOf(
            KomgaDropdown.DropdownItem("Identify", onIdentifyClick),
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
        logger.info { "seriesActions on mount" }
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
        val urlPath = document.location?.pathname?.split("/") ?: return null
        val idx = urlPath.indexOfFirst { it == "series" || it == "oneshot" }
        val seriesId = urlPath.getOrNull(idx + 1)?.let { KomfServerSeriesId(it) }
        logger.info { "detected seriesId ${seriesId?.value}" }
        return seriesId
    }


    fun getLibraryId(): KomfServerLibraryId? {
        val toolbar = document.querySelector(".v-main__wrap .v-toolbar__content") ?: return null
        val libraryId = toolbar.children.asList().find {
            val href = it.getAttribute("href") ?: return@find false
            href.contains("libraries")
        }?.getAttribute("href")?.split("/")?.getOrNull(2)

        logger.info { "detected libraryId $libraryId" }
        return libraryId?.let { KomfServerLibraryId(it) }
    }
}