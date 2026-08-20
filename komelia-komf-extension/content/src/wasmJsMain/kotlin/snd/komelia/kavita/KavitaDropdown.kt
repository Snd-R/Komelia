package snd.komelia.kavita

import kotlinx.browser.document
import kotlinx.browser.window
import org.w3c.dom.Element
import org.w3c.dom.HTMLButtonElement
import org.w3c.dom.HTMLDivElement
import org.w3c.dom.HTMLElement

class KavitaDropdown(
    parent: Element,
    items: List<DropdownItem>,
) {
    val element: HTMLDivElement = document.createElement("div") as HTMLDivElement
    var isShown = false

    init {
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

        parent.addEventListener("click") { event ->
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

        document.addEventListener("click") { event ->
            val target = event.target
            if (target is HTMLElement
                && !element.contains(target)
                && !parent.contains(target) && parent != target
            ) {
                hide()
            }
        }
    }

    fun tryMount(): Boolean {
        if (document.contains(element)) return true
        document.body?.appendChild(element)
        return true
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

