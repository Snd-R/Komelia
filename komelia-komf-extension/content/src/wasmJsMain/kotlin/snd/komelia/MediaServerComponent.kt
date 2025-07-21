package snd.komelia

import org.w3c.dom.HTMLElement

interface MediaServerComponent {
    fun tryMount(parentElement: HTMLElement): Boolean
}