package snd.komelia.image

import org.slf4j.LoggerFactory
import snd.jni.DesktopPlatform
import snd.jni.DesktopPlatform.Linux
import snd.jni.DesktopPlatform.MacOS
import snd.jni.DesktopPlatform.Unknown
import snd.jni.DesktopPlatform.Windows
import snd.jni.SharedLibrariesLoader
import snd.jni.SharedLibrariesLoader.LibraryLoadResult.BundledLibrary
import java.util.concurrent.atomic.AtomicBoolean

private const val linuxBundledLibVipsName = "vips"
private const val windowsBundledLibVipsName = "libvips-42"

object VipsSharedLibraries {
    private val logger = LoggerFactory.getLogger(VipsSharedLibraries::class.java)
    private val loaded = AtomicBoolean(false)

    @Volatile
    var isAvailable = false
        private set

    @Volatile
    var libVipsIsBundled: Boolean = false
        private set

    @Volatile
    var loadError: Throwable? = null
        private set

    private val linuxLibs = listOf(
        "z",
        "ffi",
        "glib-2.0",
        "gmodule-2.0",
        "gobject-2.0",
        "gio-2.0",
        "lcms2",
        "exif",
        "de265",
        "dav1d",
        "expat",
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
        "png",
        "tiff",
        "heif",
        linuxBundledLibVipsName,
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
        "liblcms2-2",
        "libexif-12",
        "libde265",
        "libdav1d",
        "libexpat-1",
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
        "libpng16",
        "libtiff",
        "libheif",
        windowsBundledLibVipsName,
        "libkomelia_vips",
    )

    fun load() {
        if (!loaded.compareAndSet(false, true)) return
        try {

            when (DesktopPlatform.Current) {
                Linux -> loadLinuxLibs()
                Windows -> loadLibs(windowsLibs)
                MacOS, Unknown -> error("Unsupported OS")
            }
        } catch (e: Throwable) {
            loadError = e
            throw e
        }
        VipsImage.vipsInit()
        isAvailable = true
    }

    private fun loadLibs(libs: List<String>) {
        logger.info("libraries search path: ${System.getProperty("java.library.path")}")
        for (libName in libs) {
            val loadResult = SharedLibrariesLoader.loadLibrary(libName)
            if ((libName == linuxBundledLibVipsName || libName == windowsBundledLibVipsName)
                && loadResult is BundledLibrary
            ) {
                libVipsIsBundled = true
            }
        }
    }

    private fun loadLinuxLibs() {
        try {
            loadLibs(linuxLibs)
            isAvailable = true
        } catch (e: UnsatisfiedLinkError) {
            loadRequiredLinuxLibs()
            isAvailable = true
        }
    }

    private fun loadRequiredLinuxLibs() {
        logger.info("Attempting to load only required libraries")
        try {
            SharedLibrariesLoader.loadLibrary("vips")
        } catch (e: UnsatisfiedLinkError) {
            SharedLibrariesLoader.findAndLoadFile("libvips.so.42")
        }
        SharedLibrariesLoader.loadLibrary("komelia_vips")
    }
}
