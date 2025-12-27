package snd.jni

import org.slf4j.LoggerFactory
import snd.jni.SharedLibrariesLoader.LibraryLoadResult.BundledLibrary
import snd.jni.SharedLibrariesLoader.LibraryLoadResult.SystemLibrary
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.StandardOpenOption
import kotlin.io.path.Path
import kotlin.io.path.absolutePathString
import kotlin.io.path.createDirectories
import kotlin.io.path.exists
import kotlin.io.path.listDirectoryEntries
import kotlin.io.path.name

object SharedLibrariesLoader {
    private val logger = LoggerFactory.getLogger(SharedLibrariesLoader::class.java)
    private val composeResourcesDir = System.getProperty("compose.application.resources.dir")?.let { Path(it) }
    val tempDir: Path = Path(System.getProperty("java.io.tmpdir"))
        .resolve("komelia")
        .resolve("libs")
        .createDirectories()


    @Suppress("UnsafeDynamicallyLoadedCode")
    fun findAndLoadFile(filename: String) {
        val filePath = System.getProperty("java.library.path")
            .split(":").asSequence()
            .map { Path(it) }
            .filter { it.exists() }
            .flatMap { it.listDirectoryEntries() }
            .firstOrNull { it.name == filename }

        if (filePath == null) throw UnsatisfiedLinkError("Failed to find library $filename")

        System.load(filePath.absolutePathString())
    }

    @Suppress("UnsafeDynamicallyLoadedCode")
    fun loadLibrary(libName: String): LibraryLoadResult {
        val filename = System.mapLibraryName(libName)
        try {
            if (composeResourcesDir != null) {
                val filePath = composeResourcesDir.resolve(filename)
                if (filePath.exists()) {
                    System.load(filePath.absolutePathString())
                    logger.info("loaded bundled native library $filename from resource directory")
                    return BundledLibrary
                }
            }

            val classPathFile = SharedLibrariesLoader::class.java.getResource("/${filename}")
            if (classPathFile != null) {
                val fileBytes = classPathFile.readBytes()
                val libFile = Files.write(tempDir.resolve(filename), fileBytes, StandardOpenOption.CREATE).toFile()
                libFile.deleteOnExit()
                System.load(libFile.path)
                logger.info("loaded bundled native library $filename from jar file")
                return BundledLibrary
            }

            logger.warn("$filename is not found in bundled libraries. attempting to load system library")
            System.loadLibrary(libName)
            return SystemLibrary
        } catch (e: UnsatisfiedLinkError) {
            logger.error("failed to load native library $filename", e)
            throw e
        }
    }

    sealed interface LibraryLoadResult {
        data object BundledLibrary : LibraryLoadResult
        data object SystemLibrary : LibraryLoadResult
    }
}