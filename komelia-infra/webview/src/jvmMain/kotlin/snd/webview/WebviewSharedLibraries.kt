package snd.webview

import io.github.oshai.kotlinlogging.KotlinLogging
import snd.jni.DesktopPlatform
import snd.jni.DesktopPlatform.Linux
import snd.jni.DesktopPlatform.MacOS
import snd.jni.DesktopPlatform.Unknown
import snd.jni.DesktopPlatform.Windows
import snd.jni.SharedLibrariesLoader
import snd.jni.SharedLibrariesLoader.tempDir
import java.nio.file.Files
import java.nio.file.StandardOpenOption
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.io.path.createDirectories

object WebviewSharedLibraries {
    val logger = KotlinLogging.logger {}
    private val loaded = AtomicBoolean(false)

    @Volatile
    var isAvailable = false
        private set

    fun load() {
        if (!loaded.compareAndSet(false, true)) return

        when (DesktopPlatform.Current) {
            Linux -> loadLinuxLibs()
            Windows -> SharedLibrariesLoader.loadLibrary("libkomelia_webview")
            MacOS, Unknown -> error("Unsupported OS")
        }

        isAvailable = true
    }

    private fun loadLinuxLibs() {
        // let jvm find and load libjawt. on some systems ldd might fail to find it when loading komelia_webview lib
       try {
           SharedLibrariesLoader.loadLibrary("jawt")
       } catch (e: UnsatisfiedLinkError){
           logger.catching(e)
       }

        val extensionDir = tempDir.resolve("webkit").createDirectories()
        val classPathFile = SharedLibrariesLoader::class.java.getResource("/libkomelia_webkit_extension.so")
            ?: throw UnsatisfiedLinkError("Failed to find libkomelia_webkit_extension file")
        val fileBytes = classPathFile.readBytes()
        val libFile = Files.write(
            extensionDir.resolve("libkomelia_webkit_extension.so"),
            fileBytes,
            StandardOpenOption.CREATE
        ).toFile()
        libFile.deleteOnExit()

        SharedLibrariesLoader.loadLibrary("komelia_webview")
    }
}