package snd.komelia.komga.next

import kotlinx.browser.document
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancelChildren
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import org.w3c.dom.HTMLDivElement
import org.w3c.dom.HTMLElement
import org.w3c.dom.asList
import snd.komelia.KomfActiveDialog
import snd.komelia.komga.changeTheme
import snd.komelia.ui.Theme

class KomgaSettingsButtonNext(
    theme: StateFlow<Theme>,
    private val currentDialog: MutableStateFlow<KomfActiveDialog>,
) {
    private val coroutineScope = CoroutineScope(Dispatchers.Default)
    private val element = document.createElement("div") as HTMLDivElement

    init {
        element.className =
            "v-list-item v-list-item--link v-list-item--nav v-theme--dark v-list-item--density-default v-list-item--one-line v-list-item--rounded v-list-item--variant-text"
        element.setAttribute("tabindex", "2")
        element.setAttribute("role", "listitem")
        element.innerHTML = """
            <span class="v-list-item__overlay"></span>
            <span class="v-list-item__underlay"></span>
            <div class="v-list-item__prepend">
                <i class="i-mdi:cog mdi v-icon notranslate v-theme--dark v-icon--size-default" aria-hidden="true" density="default"></i>
                <div class="v-list-item__spacer"></div>
            </div>
            <div class="v-list-item__content" data-no-activator="">
                <div class="v-list-item-title">Komf Settings</div>
            </div>
            """.trimIndent()
        element.addEventListener("click") { currentDialog.value = KomfActiveDialog.Settings }
        theme.onEach { element.changeTheme(it) }.launchIn(coroutineScope)
    }

    fun tryMount(parent: HTMLElement): Boolean {
        if (parent.contains(element)) return true

        val drawerContent = parent.getElementsByClassName("v-navigation-drawer__content").asList().toList()
            .firstOrNull { drawerNode ->
                val parent = drawerNode.parentElement
                parent != null && parent.tagName == "NAV" && !parent.classList.contains("v-navigation-drawer--temporary")
            }

        val menus = drawerContent?.children?.item(0)
        if (menus != null) {
            menus.insertBefore(element, menus.children.asList().last())
            return true
        } else return false
    }

}