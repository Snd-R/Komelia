package snd.komelia.kavita

import kotlinx.browser.document
import kotlinx.coroutines.flow.MutableStateFlow
import org.w3c.dom.Element
import org.w3c.dom.HTMLDivElement
import org.w3c.dom.HTMLElement
import org.w3c.dom.asList
import org.w3c.dom.get
import snd.komelia.KomfActiveDialog

class KavitaSettingsButton(
    private val currentDialog: MutableStateFlow<KomfActiveDialog>,
) {
    private val element: HTMLDivElement = document.createElement("div") as HTMLDivElement

    init {
        element.id = "komf-settings-button"
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

    // Kavita renders the navbar inside @if blocks (navbar visibility, logged in user, mobile breakpoint)
    // so the navbar and its items are frequently destroyed and re-created.
    // Every call checks that the button is still placed right before Kavita's own user controls and moves it back if not
    fun tryMount(parent: HTMLElement): Boolean {
        val navbar = parent.querySelector("nav.navbar .container-fluid")
        if (navbar == null) {
            element.remove()
            return false
        }

        val anchor = findAnchor(navbar)
        if (anchor == null) {
            // no user controls in navbar (login page), nothing to attach to
            element.remove()
            return false
        }

        if (element.parentElement != navbar || element.nextElementSibling != anchor) {
            navbar.insertBefore(element, anchor)
        }
        return true
    }

    private fun findAnchor(navbar: Element): Element? {
        val children = navbar.children.asList().filter { it != element }

        // settings (cog) link shown on desktop
        return children.firstOrNull { it.querySelector("i.fa-cogs") != null }
            // user profile dropdown
            ?: children.firstOrNull { it.classList.contains("dropdown") || it.hasAttribute("ngbdropdown") }
            // mobile layout profile button
            ?: children.lastOrNull { it.tagName == "BUTTON" && it.querySelector("app-profile-icon") != null }
    }
}
