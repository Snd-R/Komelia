package io.github.snd_r.komelia.fonts

import io.github.snd_r.komelia.AppDirectories
import kotlinx.io.files.Path
import java.awt.GraphicsEnvironment
import kotlin.io.path.absolutePathString

actual fun getSystemFontNames(): List<String> {
    val ge = GraphicsEnvironment.getLocalGraphicsEnvironment()
    return ge.availableFontFamilyNames.toList()
}

actual fun userFontsDirectory(): Path? {
    return Path(AppDirectories.fontDirectory.absolutePathString())
}