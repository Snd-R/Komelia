package snd.komelia.kavita

import kotlinx.browser.document
import kotlinx.browser.window
import org.w3c.dom.AddEventListenerOptions
import org.w3c.dom.Element
import org.w3c.dom.HTMLButtonElement
import org.w3c.dom.HTMLDivElement
import org.w3c.dom.HTMLElement
import kotlin.math.max

class KavitaDropdown(
    private val parent: Element,
    items: List<DropdownItem>,
) {
    val element: HTMLDivElement = document.createElement("div") as HTMLDivElement
    var isShown = false

    init {
        element.setAttribute("role", "menu")
        element.classList.value = "dropdown"
        // fixed position, coordinates are taken from getBoundingClientRect which is relative to the viewport
        element.style.position = "fixed"
        element.style.transformOrigin = "left top"
        // above page content and Kavita's fixed navbar (bootstrap uses 1030 for fixed elements)
        element.style.zIndex = "1035"
        element.style.display = "block"
        element.style.visibility = "hidden"

        val listContainer = document.createElement("div") as HTMLDivElement
        listContainer.classList.value = "dropdown-menu show"

        items.forEach { item ->
            listContainer.appendChild(createMenuItem(item.name, item.onClick))
        }
        element.appendChild(listContainer)

        parent.addEventListener("click") { event ->
            if (this.isShown) {
                this.hide()
            } else {
                val rect = parent.getBoundingClientRect()
                this.show(rect.bottom.toInt(), rect.left.toInt())
            }
        }

        document.addEventListener("click") { event ->
            val target = event.target
            if (isShown && target is HTMLElement && !element.contains(target) && !parent.contains(target)) {
                hide()
            }
        }
        // menu position is not updated on scroll, close it instead of leaving it floating in a wrong place.
        // scroll events don't bubble, listen in capture phase to also catch scrolling of inner containers
        document.addEventListener(
            type = "scroll",
            callback = { if (isShown) hide() },
            options = AddEventListenerOptions(capture = true, passive = true)
        )
        window.addEventListener("resize", { if (isShown) hide() })
    }

    fun tryMount(): Boolean {
        if (element.isConnected) return true
        document.body?.appendChild(element)
        return true
    }

    fun show(top: Int, left: Int) {
        val menuWidth = element.firstElementChild?.getBoundingClientRect()?.width?.toInt() ?: 0
        val maxLeft = window.innerWidth - menuWidth - 8
        element.style.top = "${top}px"
        element.style.left = "${max(0, minOf(left, maxLeft))}px"
        element.style.visibility = "visible"
        isShown = true
    }

    fun hide() {
        if (!isShown) return
        element.style.visibility = "hidden"
        isShown = false
    }

    private fun createMenuItem(title: String, onClick: () -> Unit): HTMLButtonElement {
        val item = document.createElement("button") as HTMLButtonElement
        item.classList.value = "dropdown-item"
        item.addEventListener("click") { event ->
            hide()
            onClick()
        }
        item.innerText = title
        return item
    }

    data class DropdownItem(
        val name: String,
        val onClick: () -> Unit,
    )
}
