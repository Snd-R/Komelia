package snd.komelia.komga

import kotlinx.browser.document
import org.w3c.dom.HTMLDivElement


class KomgaDropdown(
    items: List<DropdownItem>,
) {
    val element: HTMLDivElement
    var isShown = false

    init {
        element = document.createElement("div") as HTMLDivElement
        element.setAttribute("role", "menu")
        element.classList.value = "v-menu__content theme--dark menuable__content__active"
        element.style.minWidth = "48px"
        element.style.transformOrigin = "left top"
        element.style.zIndex = "8"
        element.style.display = "none"

        val listContainer = document.createElement("div") as HTMLDivElement
        listContainer.classList.value = "v-list v-sheet theme--dark v-list--dense"

        items.forEach { item ->
            listContainer.appendChild(createMenuItem(item.name, item.onClick))
        }
        element.appendChild(listContainer)
    }

    fun show(top: Int, left: Int) {
        element.style.display = "block"
        element.style.top = "${top}px"
        element.style.left = "${left}px"
        isShown = true
    }

    fun hide() {
        element.style.display = "none"
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

