package snd.komelia.offline.mediacontainer

import com.github.junrar.Archive
import io.github.vinceglb.filekit.AndroidFile
import io.github.vinceglb.filekit.FileKit
import io.github.vinceglb.filekit.PlatformFile
import io.github.vinceglb.filekit.context
import java.io.FileInputStream

class RarExtractor : DivinaExtractor {

    override fun mediaTypes(): List<String> = listOf(
        "application/x-rar-compressed",
        "application/x-rar-compressed; version=4",
        "application/x-rar-compressed; version=5"
    )

    override fun getEntryBytes(
        file: PlatformFile,
        entryName: String
    ): ByteArray {
        return when (val androidFile = file.androidFile) {
            is AndroidFile.FileWrapper -> Archive(androidFile.file).extractEntryAndClose(entryName)
            is AndroidFile.UriWrapper -> {
                val pfd = FileKit.context.contentResolver.openFileDescriptor(androidFile.uri, "r")
                    ?: error("Failed to open file descriptor ${androidFile.uri}")
                Archive(FileInputStream(pfd.fileDescriptor)).extractEntryAndClose(entryName)
            }
        }
    }

    private fun Archive.extractEntryAndClose(entryName: String): ByteArray {
        return this.use { rar ->
            val header = rar.fileHeaders.find { it.fileName == entryName }
            rar.getInputStream(header).use { it.readBytes() }
        }
    }
}