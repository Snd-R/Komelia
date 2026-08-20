package snd.komelia.komga

import kotlinx.browser.document
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import org.w3c.dom.HTMLDivElement
import org.w3c.dom.HTMLElement
import org.w3c.dom.asList
import org.w3c.dom.get
import snd.komelia.KomfActiveDialog
import snd.komelia.ui.Theme

class KomgaSettingsButton(
    theme: StateFlow<Theme>,
    private val currentDialog: MutableStateFlow<KomfActiveDialog>,
) {
    private val coroutineScope = CoroutineScope(Dispatchers.Default)

    val element = document.createElement("div") as HTMLDivElement

    init {
        element.className = "v-list-group v-list-group--no-action"
        element.innerHTML = """
<div tabindex="0" aria-expanded="false" role="button" class="v-list-group__header v-list-item v-list-item--link theme--dark">
   <div class="v-list-item__icon v-list-group__header__prepend-icon"><i aria-hidden="true" class="v-icon notranslate mdi mdi-puzzle theme--dark"></i></div>
   <div class="v-list-item__title">Komf settings</div>
</div>
"""
        element.addEventListener("click") {
            currentDialog.value = KomfActiveDialog.Settings
        }
        (element.children[0] as HTMLElement).addEventListener("focus") { event ->
            (event.target as HTMLElement).blur()
        }

        theme.onEach { element.changeTheme(it) }.launchIn(coroutineScope)
    }

    fun tryMount(parent: HTMLElement): Boolean {
        if (parent.contains(element)) return true

        val drawerContent = parent.getElementsByClassName("v-navigation-drawer__content").asList()
            .find { drawerNode -> drawerNode.parentElement?.tagName == "NAV" }
        val menus = drawerContent?.children?.item(2)
        if (menus != null) {
            menus.insertBefore(element, menus.children.asList().last())
            return true
        } else return false
    }
}