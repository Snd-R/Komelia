package snd.webview

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