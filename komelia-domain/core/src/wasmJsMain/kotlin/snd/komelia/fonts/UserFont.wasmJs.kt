package snd.komelia.fonts

import kotlinx.io.files.Path

actual fun getSystemFontNames(): List<String> {
    return emptyList()
}

actual fun userFontsDirectory(): Path? {
    return null
}