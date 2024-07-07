package io.github.snd_r

object AndroidSharedLibrariesLoader {
    private val androidLibs = listOf(
        "z",
        "ffi",
        "intl",
        "iconv",
        "omp",
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
        "komelia_android_bitmap",
    )

    @Synchronized
    fun load() {
        androidLibs.forEach { System.loadLibrary(it) }
        VipsImage.vipsInit()
    }
}