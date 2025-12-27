package snd.komelia.fonts

import kotlinx.io.files.Path
import kotlinx.io.files.SystemTemporaryDirectory

actual fun getSystemFontNames() = emptyList<String>()

var fontsDirectory = SystemTemporaryDirectory
actual fun userFontsDirectory(): Path? {
    return fontsDirectory
}