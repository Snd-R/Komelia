package snd.komelia.kavita

import kotlinx.browser.document
import kotlinx.coroutines.flow.MutableStateFlow
import org.w3c.dom.HTMLDivElement
import org.w3c.dom.HTMLElement
import org.w3c.dom.get
import snd.komelia.KomfActiveDialog

class KavitaSettingsButton(
    private val currentDialog: MutableStateFlow<KomfActiveDialog>,
) {
    private val element: HTMLDivElement = document.createElement("div") as HTMLDivElement

    init {
        element.className = "nav-item"
        element.title = "Komf Settings"
        element.innerHTML = """
<button type="button" placement="bottom" class="btn btn-icon" title="Komf Settings">
    <i aria-hidden="true" class="fa fa-puzzle-piece nav"></i>
</button>
"""
        element.addEventListener("click") { event ->
            currentDialog.value = KomfActiveDialog.Settings
        }
        (element.children[0] as HTMLElement).addEventListener("focus") { event ->
            (event.target as HTMLElement).blur()
        }
    }

    fun tryMount(parent: HTMLElement): Boolean {
        if (parent.contains(element)) return true

        val navbar = parent.getElementsByTagName("nav")[0]?.firstElementChild

        if (navbar != null) {
            navbar.insertBefore(element, navbar.children[4])
            return true
        }

        return false

    }
}