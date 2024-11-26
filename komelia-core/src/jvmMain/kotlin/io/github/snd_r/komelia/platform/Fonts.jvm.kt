package io.github.snd_r.komelia.platform

import java.awt.GraphicsEnvironment

actual fun getSystemFontNames(): List<String> {
    val ge = GraphicsEnvironment.getLocalGraphicsEnvironment()
    return ge.availableFontFamilyNames.toList()
}