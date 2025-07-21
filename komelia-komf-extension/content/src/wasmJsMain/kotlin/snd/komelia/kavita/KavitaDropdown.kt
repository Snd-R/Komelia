package snd.komelia.kavita

import kotlinx.browser.document
import kotlinx.browser.window
import org.w3c.dom.HTMLButtonElement
import org.w3c.dom.HTMLDivElement


class KavitaDropdown(
    items: List<DropdownItem>,
) {
    val element: HTMLDivElement
    var isShown = false

    init {
        element = document.createElement("div") as HTMLDivElement
        element.setAttribute("role", "menu")
        element.classList.value = "dropdown"
        element.style.position = "absolute"
        element.style.transformOrigin = "left top"
        element.style.zIndex = "8"
        element.style.display = "block"
        element.style.visibility = "hidden"

        val listContainer = document.createElement("div") as HTMLDivElement
        listContainer.classList.value = "dropdown-menu show"

        items.forEach { item ->
            listContainer.appendChild(createMenuItem(item.name, item.onClick))
        }
        element.appendChild(listContainer)
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

    private fun createMenuItem(title: String, onClick: () -> Unit): HTMLButtonElement {
        val item = document.createElement("button") as HTMLButtonElement
        item.classList.value = "dropdown-item"
        item.addEventListener("click") { event -> onClick() }
        item.innerText = title
        return item
    }

    data class DropdownItem(
        val name: String,
        val onClick: () -> Unit,
    )
}

