package snd.komelia.komga

import io.github.snd_r.komelia.ui.common.AppTheme
import kotlinx.browser.document
import kotlinx.browser.localStorage
import kotlinx.browser.window
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import org.w3c.dom.HTMLDivElement
import org.w3c.dom.HTMLElement
import org.w3c.dom.asList
import org.w3c.dom.get
import snd.komelia.KomfActiveDialog
import snd.komelia.MediaServerComponent
import snd.komelia.logger

class KomgaComponent(
    private val theme: MutableStateFlow<AppTheme>,
    private val currentDialog: MutableStateFlow<KomfActiveDialog>,
) : MediaServerComponent {
    private val settingsButton: HTMLDivElement = document.createElement("div") as HTMLDivElement
    private val seriesActions = KomgaSeriesActions(theme = theme, currentDialog = currentDialog)
    private val libraryActions = KomgaLibraryActions(theme = theme, currentDialog = currentDialog)

    init {
        settingsButton.className = "v-list-group v-list-group--no-action"
        settingsButton.innerHTML = """
<div tabindex="0" aria-expanded="false" role="button" class="v-list-group__header v-list-item v-list-item--link theme--dark">
   <div class="v-list-item__icon v-list-group__header__prepend-icon"><i aria-hidden="true" class="v-icon notranslate mdi mdi-puzzle theme--dark"></i></div>
   <div class="v-list-item__title">Komf settings</div>
</div>
"""
        settingsButton.addEventListener("click") { event ->
            currentDialog.value = KomfActiveDialog.Settings
        }
        (settingsButton.children[0] as HTMLElement).addEventListener("focus") { event ->
            (event.target as HTMLElement).blur()
        }
    }

    fun launch() {
        localStorage.getItem("vuex")?.let {
            val json = Json.decodeFromString<JsonObject>(it)
            val persistedState = json["persistedState"] as? JsonObject
            val komgaTheme = persistedState?.get("theme") as? JsonPrimitive
            if (komgaTheme != null) {
                when (komgaTheme.content) {
                    "theme.dark" -> this.theme.value = AppTheme.DARK
                    "theme.system" -> {
                        if (window.matchMedia("(prefers-color-scheme: dark)").matches)
                            this.theme.value = AppTheme.DARK
                        else this.theme.value = AppTheme.LIGHT

                    }

                    else -> this.theme.value = AppTheme.LIGHT
                }
            }
        }

        if (theme.value == AppTheme.LIGHT) {
            settingsButton.getElementsByClassName("theme--dark").asList().toList().forEach { elem ->
                elem.classList.replace("theme--dark", "theme--light")
            }
        }
    }


    override fun tryMount(parentElement: HTMLElement): Boolean {
        var mounted = false
        val drawerContent = parentElement.getElementsByClassName("v-navigation-drawer__content").asList()
        val menus = drawerContent
            .find { drawerNode -> drawerNode.parentElement?.tagName == "NAV" }
            ?.children?.item(2)

        if (menus != null) {
            logger.info { "detected settings button mount point" }
            menus.insertBefore(settingsButton, menus.children.asList().last())
            mounted = true
            libraryActions.onMount()
            seriesActions.onMount()
        }

        val toolbar = parentElement.querySelector(".v-main__wrap .v-toolbar__content")
        val toolbarParent = toolbar?.parentElement
        if (toolbar != null && toolbarParent != null && !toolbarParent.classList.contains("hidden-sm-and-up")) {
            val path = window.location.pathname.split("/").reversed()
            logger.info { "detecting current screen from url; current path: $path" }
            if (path.any { it == "libraries" }) {
                logger.info { "detected library screen; mounting library actions" }
                toolbar.children[4]?.insertAdjacentElement("afterend", libraryActions.element)
            } else if (path.any { it == "series" }) {
                logger.info { "detected series screen; mounting series actions" }
                toolbar.children[4]?.insertAdjacentElement("afterend", seriesActions.element)
            } else if (path.any { it == "oneshot" }) {
                logger.info { "detected oneshot screen; mounting series actions" }
                toolbar.children.asList()
                    .find { it.tagName == "BUTTON" }
                    ?.insertAdjacentElement("afterend", seriesActions.element)
            }
        }
        return mounted
    }
}
