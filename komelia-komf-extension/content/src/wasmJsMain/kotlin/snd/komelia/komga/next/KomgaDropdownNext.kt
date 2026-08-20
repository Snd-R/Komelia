package snd.komelia.komga.next

import kotlinx.browser.document
import kotlinx.browser.window
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import org.w3c.dom.Element
import org.w3c.dom.HTMLDivElement
import org.w3c.dom.HTMLElement
import org.w3c.dom.asList
import snd.komelia.komga.changeTheme
import snd.komelia.ui.Theme
import kotlin.math.roundToInt

class KomgaDropdownNext(
    parent: Element,
    items: List<DropdownItem>,
    theme: StateFlow<Theme>,
) {
    private val coroutineScope = CoroutineScope(Dispatchers.Default)
    private val overlayElement: HTMLDivElement
    private val containerElement: HTMLDivElement
    var isShown = false
        private set

    init {
        overlayElement = document.createElement("div") as HTMLDivElement
        overlayElement.classList.value =
            "v-overlay v-overlay--absolute v-overlay--active v-theme--dark v-locale--is-ltr v-menu"
        overlayElement.style.zIndex = "2000"

        containerElement = document.createElement("div") as HTMLDivElement
        containerElement.classList.value = "v-overlay__content"
        containerElement.setAttribute("style", "--v-overlay-anchor-origin: bottom left;")
        containerElement.style.minWidth = "48px"
        containerElement.style.transformOrigin = "left top"
        containerElement.style.display = "block"
        containerElement.style.visibility = "hidden"
        overlayElement.appendChild(containerElement)

        val listContainer = document.createElement("div") as HTMLDivElement
        listContainer.classList.value = "v-list v-theme--dark v-list--density-compact v-list--one-line"
        listContainer.setAttribute(
            "style",
            "--v-list-indent: 40px; --v-list-group-prepend: 0px; --v-list-prepend-gap: 16px;"
        )

        items.forEach { item ->
            listContainer.appendChild(createMenuItem(item.name, item.onClick))
        }
        containerElement.appendChild(listContainer)

        document.addEventListener("click") { event ->
            val target = event.target

            if (target is HTMLElement
                && !containerElement.contains(target)
                && !parent.contains(target)
                && parent != target
            ) {
                this.hide()
            }
        }

        parent.addEventListener("click") {
            val rect = parent.getBoundingClientRect()
            if (this.isShown) {
                this.hide()
            } else {
                this.show(
                    rect.bottom.toInt(),
                    rect.left.toInt()
                )
            }
        }

        theme.onEach { overlayElement.changeTheme(it) }.launchIn(coroutineScope)
    }

    fun show(top: Int, left: Int) {
        if (window.innerWidth < left + containerElement.firstElementChild!!.getBoundingClientRect().width) {
            val offset =
                window.innerWidth - containerElement.firstElementChild!!.getBoundingClientRect().width.roundToInt() - 1
            containerElement.style.left = "${offset}px"
        } else {
            containerElement.style.left = "${left}px"
        }
        containerElement.style.visibility = "visible"
        containerElement.style.top = "${top}px"
        isShown = true
    }

    fun hide() {
        containerElement.style.visibility = "hidden"
        isShown = false
    }

    fun tryMount() {
        if (document.contains(overlayElement)) return

        document.getElementsByClassName("v-overlay-container").asList().firstOrNull()
            ?.appendChild(overlayElement)
            ?: return
    }

    private fun createMenuItem(title: String, onClick: () -> Unit): HTMLDivElement {
        val item = document.createElement("div") as HTMLDivElement
        item.setAttribute("role", "listitem")
        item.tabIndex = -2
        item.classList.value =
            "v-list-item v-list-item--link v-theme--dark v-list-item--density-compact v-list-item--one-line rounded-0 v-list-item--variant-text"
        item.addEventListener("click") { onClick() }

        val itemOverlay = document.createElement("span")
        itemOverlay.classList.value = "v-list-item__overlay"
        item.appendChild(itemOverlay)
        val itemUnderlay = document.createElement("span")
        itemUnderlay.classList.value = "v-list-item__underlay"
        item.appendChild(itemUnderlay)

        val itemContent = document.createElement("div") as HTMLDivElement
        itemContent.classList.value = "v-list-item__content"
        item.appendChild(itemContent)

        val itemTitle = document.createElement("div") as HTMLDivElement
        itemTitle.classList.value = "v-list-item-title"
        itemTitle.innerText = title
        itemContent.appendChild(itemTitle)

        return item
    }

    data class DropdownItem(
        val name: String,
        val onClick: () -> Unit,
    )
}
