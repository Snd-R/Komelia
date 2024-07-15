package io.github.snd_r

import io.github.snd_r.DesktopPlatform.Linux
import io.github.snd_r.DesktopPlatform.MacOS
import io.github.snd_r.DesktopPlatform.Unknown
import io.github.snd_r.DesktopPlatform.Windows
import org.jetbrains.skia.Bitmap
import java.util.concurrent.atomic.AtomicBoolean

object VipsBitmapFactory {
    private val loaded = AtomicBoolean(false)

    fun load() {
        if (!loaded.compareAndSet(false, true)) return

        when (DesktopPlatform.Current) {
            Linux -> SharedLibrariesLoader.loadLibrary("komelia_skia")
            Windows -> SharedLibrariesLoader.loadLibrary("libkomelia_skia")
            MacOS, Unknown -> error("Unsupported OS")
        }
    }

    external fun createSkiaBitmap(image: VipsImage): Bitmap
}