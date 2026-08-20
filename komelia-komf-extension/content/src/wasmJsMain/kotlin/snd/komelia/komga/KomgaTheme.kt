package snd.komelia.komga

import kotlinx.browser.localStorage
import kotlinx.browser.window
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import org.w3c.dom.Element
import org.w3c.dom.asList
import snd.komelia.ui.Theme

private const val darkThemeClass = "theme--dark"
private const val lightThemeClass = "theme--light"

fun Element.changeTheme(theme: Theme) {
    val (from, to) = when (theme) {
        Theme.DARK, Theme.DARKER -> lightThemeClass to darkThemeClass
        Theme.LIGHT -> darkThemeClass to lightThemeClass
    }
    (this.getElementsByClassName(from).asList().toList() + this)
        .forEach { it.classList.replace(from, to) }
}

fun getCurrentTheme(): Theme {
    val localStorageSettings = localStorage.getItem("vuex") ?: return Theme.LIGHT
    val persistedState = Json.decodeFromString<JsonObject>(localStorageSettings)["persistedState"] as? JsonObject
    val komgaTheme = persistedState?.get("theme") as? JsonPrimitive

    return when (komgaTheme?.content) {
        "theme.dark" -> Theme.DARK
        "theme.system" -> {
            if (window.matchMedia("(prefers-color-scheme: dark)").matches) Theme.DARK
            else Theme.LIGHT

        }

        else -> Theme.LIGHT
    }
}