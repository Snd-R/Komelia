package snd.komelia.komga

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
import snd.komelia.ui.Theme

class KomgaDropdown(
    parent: Element,
    items: List<DropdownItem>,
    theme: StateFlow<Theme>,
) {
    private val element: HTMLDivElement = document.createElement("div") as HTMLDivElement
    private val coroutineScope = CoroutineScope(Dispatchers.Default)
    var isShown = false

    init {
        element.setAttribute("role", "menu")
        element.classList.value = "v-menu__content theme--dark menuable__content__active"
        element.style.minWidth = "48px"
        element.style.transformOrigin = "left top"
        element.style.zIndex = "8"
        element.style.display = "block"
        element.style.visibility = "hidden"

        val listContainer = document.createElement("div") as HTMLDivElement
        listContainer.classList.value = "v-list v-sheet theme--dark v-list--dense"

        items.forEach { item ->
            listContainer.appendChild(createMenuItem(item.name, item.onClick))
        }
        element.appendChild(listContainer)

        theme.onEach { element.changeTheme(it) }.launchIn(coroutineScope)


        document.addEventListener("click") { event ->
            val target = event.target

            if (target is HTMLElement
                && !element.contains(target)
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
    }

    fun tryMount(): Boolean {
        if (document.contains(element)) return true
        val appElement = document.getElementById("app")
        appElement?.appendChild(element)
        return appElement != null
    }

    fun show(top: Int, left: Int) {
        val leftOffset =
            if (window.innerWidth < left + element.firstElementChild!!.getBoundingClientRect().width) {
                window.innerWidth - left
            } else 0
        element.style.visibility = "visible"
        element.style.top = "${top}px"
        element.style.left = "${left - leftOffset}px"
        isShown = true
    }

    fun hide() {
        element.style.visibility = "hidden"
        isShown = false
    }

    private fun createMenuItem(title: String, onClick: () -> Unit): HTMLDivElement {
        val item = document.createElement("div") as HTMLDivElement
        item.setAttribute("role", "menuitem")
        item.classList.value = "v-list-item v-list-item--link theme--dark"
        item.addEventListener("click") { event -> onClick() }

        val itemTitle = document.createElement("div") as HTMLDivElement
        itemTitle.classList.value = "v-list-item__title"
        itemTitle.innerText = title
        item.appendChild(itemTitle)

        return item
    }

    data class DropdownItem(
        val name: String,
        val onClick: () -> Unit,
    )
}
