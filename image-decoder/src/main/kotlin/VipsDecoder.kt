package io.github.snd_r

import io.github.snd_r.DesktopPlatform.Linux
import io.github.snd_r.DesktopPlatform.MacOS
import io.github.snd_r.DesktopPlatform.Unknown
import io.github.snd_r.DesktopPlatform.Windows
import org.slf4j.LoggerFactory
import java.util.concurrent.atomic.AtomicBoolean

object VipsDecoder {
    private val logger = LoggerFactory.getLogger(VipsDecoder::class.java)
    private val loaded = AtomicBoolean(false)
    var isAvailable = false
        private set


    private val linuxRequiredLibs = listOf(
        "glib-2.0",
        "gobject-2.0",
        "vips",
        "komelia_vips",
    )
    private val linuxLibs = listOf(
        "z",
        "ffi",
        "glib-2.0",
        "gmodule-2.0",
        "gobject-2.0",
        "gio-2.0",
        "de265",
        "dav1d",
        "expat",
        "fftw3",
        "hwy",
        "sharpyuv",
        "webp",
        "webpdecoder",
        "webpdemux",
        "webpmux",
        "jpeg",
        "brotlicommon",
        "brotlidec",
        "brotlienc",
        "jxl_cms",
        "jxl_threads",
        "jxl",
        "spng",
        "tiff",
        "heif",
        "vips",
        "komelia_vips",
    )

    private val windowsLibs = listOf(
        "libwinpthread-1",
        "libgcc_s_seh-1",
        "libstdc++-6",
        "libz1",
        "libffi-8",
        "libintl-8",
        "libglib-2.0-0",
        "libgmodule-2.0-0",
        "libgobject-2.0-0",
        "libgio-2.0-0",
        "libde265",
        "libdav1d",
        "libexpat-1",
        "libfftw3",
        "libhwy",
        "libsharpyuv",
        "libwebp",
        "libwebpdecoder",
        "libwebpdemux",
        "libwebpmux",
        "libjpeg-62",
        "libbrotlicommon",
        "libbrotlidec",
        "libbrotlienc",
        "libjxl_cms",
        "libjxl_threads",
        "libjxl",
        "libspng",
        "libtiff",
        "libheif",
        "libvips-42",
        "libkomelia_vips",
    )

    @Synchronized
    fun load() {
        if (!loaded.compareAndSet(false, true)) return
        when (DesktopPlatform.Current) {
            Linux -> loadLinuxLibs()
            Windows -> loadLibs(windowsLibs)
            MacOS, Unknown -> error("Unsupported OS")
        }
        init()
        isAvailable = true
    }

    private fun loadLinuxLibs() {
        try {
            loadLibs(linuxLibs)
            isAvailable = true
        } catch (e: UnsatisfiedLinkError) {
            loadLibs(linuxRequiredLibs)
            isAvailable = true
        }
    }

    private fun loadLibs(libs: List<String>) {
        logger.info("libraries search path: ${System.getProperty("java.library.path")}")
        for (libName in libs) {
            SharedLibrariesLoader.loadLibrary(libName)
        }
    }

    private external fun init()
}