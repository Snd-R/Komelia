package snd.komelia.offline.sync

import io.github.vinceglb.filekit.PlatformFile
import io.github.vinceglb.filekit.delete
import kotlinx.io.Sink
import kotlinx.io.asOutputStream
import kotlinx.io.buffered
import kotlinx.io.files.SystemFileSystem
import snd.jni.DesktopPlatform
import java.util.zip.Deflater
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream
import kotlin.io.path.createDirectories

internal actual suspend fun prepareOutput(
    downloadRoot: PlatformFile,
    serverName: String,
    libraryName: String,
    seriesName: String,
    bookFileName: String,
): Pair<PlatformFile, Sink> {

    val filePath = when (DesktopPlatform.Current) {
        DesktopPlatform.Windows -> downloadRoot.file.toPath()
            .resolve(serverName.removeIllegalWindowsPathChars())
            .resolve(libraryName.removeIllegalWindowsPathChars())
            .resolve(seriesName.removeIllegalWindowsPathChars())
            .resolve(bookFileName.removeIllegalWindowsPathChars())

        else -> downloadRoot.file.toPath()
            .resolve(serverName)
            .resolve(libraryName)
            .resolve(seriesName)
            .resolve(bookFileName)
    }

    filePath.parent.createDirectories()

    val kotlinxIoPath = kotlinx.io.files.Path(filePath.toString())
    return PlatformFile(filePath.toFile()) to SystemFileSystem.sink(kotlinxIoPath).buffered()
}


private val windowsReservedChars = "[<>:\"/|?*\u0000-\u001F]|[. ]$".toRegex()
private fun String.removeIllegalWindowsPathChars(): String {
    return this.replace(windowsReservedChars, "")
}


internal actual suspend fun deleteFile(file: PlatformFile) {
    file.delete(false)
}

internal actual class ZipWriter actual constructor(output: Sink) {
    private val zip = ZipOutputStream(output.asOutputStream()).apply { setLevel(Deflater.NO_COMPRESSION) }

    actual fun putEntry(name: String, bytes: ByteArray) {
        zip.putNextEntry(ZipEntry(name))
        zip.write(bytes)
        zip.closeEntry()
    }

    actual fun finish() = zip.finish()
}