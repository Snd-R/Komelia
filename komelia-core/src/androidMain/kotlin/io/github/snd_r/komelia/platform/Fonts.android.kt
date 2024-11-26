package io.github.snd_r.komelia.platform

import android.graphics.fonts.SystemFonts
import android.os.Build
import java.io.File

actual fun getSystemFontNames(): List<String> {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
        SystemFonts.getAvailableFonts().mapNotNull {
            it.file?.nameWithoutExtension?.replace("-", " ")
        }
    } else {
        File("/system/fonts").listFiles()?.map { it.nameWithoutExtension.replace("-", " ") } ?: emptyList()
    }
}