package io.github.snd_r

import org.slf4j.LoggerFactory
import snd.jni.SharedLibrariesLoader
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.StandardOpenOption
import kotlin.io.path.Path
import kotlin.io.path.absolutePathString
import kotlin.io.path.createDirectories
import kotlin.io.path.exists

object SharedLibrariesLoader {
    private val logger = LoggerFactory.getLogger(SharedLibrariesLoader::class.java)
    private val composeResourcesDir = System.getProperty("compose.application.resources.dir")?.let { Path(it) }
    internal val tempDir: Path = Path(System.getProperty("java.io.tmpdir"))
        .resolve("komelia")
        .resolve("libs")
        .createDirectories()

    @Suppress("UnsafeDynamicallyLoadedCode")
    fun loadLibrary(libName: String) {
        try {
            val filename = System.mapLibraryName(libName)

            if (composeResourcesDir != null) {
                val filePath = composeResourcesDir.resolve(filename)
                if (filePath.exists()) {
                    System.load(filePath.absolutePathString())
                    logger.info("loaded bundled native library $filename from resource directory")
                    return
                }
            }

            val classPathFile = SharedLibrariesLoader::class.java.getResource("/${filename}")
            if (classPathFile != null) {
                val fileBytes = classPathFile.readBytes()
                val libFile = Files.write(tempDir.resolve(filename), fileBytes, StandardOpenOption.CREATE).toFile()
                libFile.deleteOnExit()
                System.load(libFile.path)
                logger.info("loaded bundled native library $filename from jar file")
                return
            }

            logger.warn("$filename is not found in bundled libraries. attempting to load system library")
            System.loadLibrary(libName)
        } catch (e: UnsatisfiedLinkError) {
            logger.error("failed to load native library $libName")
            throw e
        }
    }
}