package snd.komelia.kavita

import io.github.oshai.kotlinlogging.KotlinLogging
import io.github.snd_r.komelia.ui.common.AppTheme
import kotlinx.browser.document
import kotlinx.coroutines.flow.MutableStateFlow
import org.w3c.dom.HTMLDivElement
import org.w3c.dom.HTMLElement
import org.w3c.dom.asList
import org.w3c.dom.get
import snd.komelia.KomfActiveDialog
import snd.komelia.MediaServerComponent

private val logger = KotlinLogging.logger { }

class KavitaComponent(
    private val theme: MutableStateFlow<AppTheme>,
    private val currentDialog: MutableStateFlow<KomfActiveDialog>,
) : MediaServerComponent {
    private val settingsButton: HTMLDivElement = document.createElement("div") as HTMLDivElement
    private val seriesActions = KavitaSeriesActions(currentDialog = currentDialog)
    private val libraryActions = KavitaLibraryActions(currentDialog = currentDialog)

    init {
        settingsButton.className = "nav-item"
        settingsButton.title = "Komf Settings"
        settingsButton.innerHTML = """
<button type="button" placement="bottom" class="btn btn-icon" title="Komf Settings">
    <i aria-hidden="true" class="fa fa-puzzle-piece nav"></i>
</button>
"""
        settingsButton.addEventListener("click") { event ->
            currentDialog.value = KomfActiveDialog.Settings
        }
        (settingsButton.children[0] as HTMLElement).addEventListener("focus") { event ->
            (event.target as HTMLElement).blur()
        }
    }

    fun launch() {
        theme.value = AppTheme.DARK
    }


    override fun tryMount(parentElement: HTMLElement): Boolean {
        var mounted = false
        val navbar = parentElement.getElementsByTagName("nav")[0]?.firstElementChild
        if (navbar != null) {
            navbar.insertBefore(settingsButton, navbar.children[4])
            libraryActions.onMount()
            seriesActions.onMount()
            mounted = true
        }

        val buttons = parentElement.getElementsByTagName("button").asList()
        val editSeriesButton = buttons.find { it.getAttribute("id") == "edit-btn--komf" }
        val libraryFilterButton = buttons.find { it.getAttribute("id") == "filter-btn--komf" }

        if (editSeriesButton != null) {
            logger.info { "Mounting series actions" }
            editSeriesButton.parentElement?.insertAdjacentElement("afterend", seriesActions.element)
//            editSeriesButton.append(seriesActions.element)

        }
        if (libraryFilterButton != null) {
            logger.info { "Mounting library actions" }
            libraryFilterButton.parentElement?.append(libraryActions.element)
        }
        return mounted
    }
}
