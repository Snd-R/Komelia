package io.github.snd_r.komelia.ui.platform

import java.awt.Toolkit
import java.awt.datatransfer.StringSelection

actual fun copyToClipboard(text: String) {
    Toolkit.getDefaultToolkit().systemClipboard
        .setContents(StringSelection(text), null)
}
