package io.github.snd_r

import org.slf4j.LoggerFactory
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.StandardOpenOption
import kotlin.io.path.Path
import kotlin.io.path.absolutePathString
import kotlin.io.path.createDirectories
import kotlin.io.path.exists

object SharedLibrariesLoader {
    private val logger = LoggerFactory.getLogger(VipsDecoder::class.java)
    private val javaLibPath: List<Path> = System.getProperty("java.library.path").ifBlank { null }
        ?.let { path -> path.split(":").map { Path.of(it) } }
        ?: emptyList()
    internal val tempDir: Path = Path(System.getProperty("java.io.tmpdir")).resolve("komelia_libs").createDirectories()

    @Suppress("UnsafeDynamicallyLoadedCode")
    fun loadLibrary(libName: String) {
        try {
            val filename = System.mapLibraryName(libName)
            val classPathFileBytes = VipsDecoder::class.java.getResource("/${filename}")?.readBytes()

            val javaPathFile =
                if (classPathFileBytes == null)
                    javaLibPath.map { it.resolve(filename) }.firstOrNull { it.exists() }
                else null

            when {
                classPathFileBytes == null && javaPathFile == null -> {
                    logger.warn("$filename is not found in bundled libraries. attempting to load system library")
                    System.loadLibrary(libName)
                }

                classPathFileBytes != null -> {
                    val libFile =
                        Files.write(tempDir.resolve(filename), classPathFileBytes, StandardOpenOption.CREATE).toFile()
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