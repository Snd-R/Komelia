package io.github.snd_r

import org.slf4j.LoggerFactory
import java.nio.file.Files
import java.nio.file.Path
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.io.path.absolutePathString
import kotlin.io.path.createTempDirectory
import kotlin.io.path.exists


object VipsDecoder {
    private val logger = LoggerFactory.getLogger(VipsDecoder::class.java)
    private val osName: String = System.getProperty("os.name")
    private val javaLibPath: List<Path> = System.getProperty("java.library.path").ifBlank { null }
        ?.let { path -> path.split(":").map { Path.of(it) } }
        ?: emptyList()
    private var loaded = AtomicBoolean(false)

    private var tempDir: Path? = null
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
        "komelia",
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
        "libkomelia",
    )

    @Synchronized
    fun load() {
        if (!loaded.compareAndSet(false, true)) return
        when {
            osName.startsWith("linux", true) -> loadLibs(linuxLibs)
            osName.startsWith("windows", true) -> loadLibs(windowsLibs)
//            osName.startsWith("Mac OS X", true) -> {}
            else -> throw IllegalStateException("Unsupported OS")
        }

        init()
    }

    @Suppress("UnsafeDynamicallyLoadedCode")
    private fun loadLibs(libs: List<String>) {
        logger.info("libraries search path: ${System.getProperty("java.library.path")}")
        for (libName in libs) {
            try {
                val filename = System.mapLibraryName(libName)
                val classPathFileBytes = VipsDecoder::class.java.getResource("/${filename}")?.readBytes()

                val javaPathFile =
                    if (classPathFileBytes == null)
                        javaLibPath.map { it.resolve(filename) }.firstOrNull { it.exists() }
                    else null

                when {
                    classPathFileBytes == null && javaPathFile == null -> {
                        logger.warn("$filename is not found in bundled libraries. loading system library")
                        System.loadLibrary(libName)
                    }

                    classPathFileBytes != null -> {
                        val tempDir = tempDir ?: createTempDirectory(prefix = "komelia").also { tempDir = it }

                        val libFile = Files.write(tempDir.resolve(filename), classPathFileBytes).toFile()
                        libFile.deleteOnExit()
                        System.load(libFile.path)
                        logger.info("loaded bundled native library $filename")
                    }

                    javaPathFile != null -> {
                        System.load(javaPathFile.absolutePathString())
                        logger.info("loaded native library from java path $filename")
                    }
                }

            } catch (e: UnsatisfiedLinkError) {
                logger.error("failed to load native library $libName")
                throw e
            }
        }
    }

    private external fun init()

    external fun vipsDecode(encoded: ByteArray): VipsImage?
    external fun vipsDecodeAndResize(encoded: ByteArray, scaleWidth: Int, scaleHeight: Int, crop: Boolean): VipsImage?
}