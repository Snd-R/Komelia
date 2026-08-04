package snd.komelia.offline.mediacontainer.divina

import com.github.junrar.Archive
import io.github.vinceglb.filekit.PlatformFile
import snd.komelia.offline.mediacontainer.DivinaExtractor

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
        return Archive(file.file).use { rar ->
            val header = rar.fileHeaders.find { it.fileName == entryName }
            rar.getInputStream(header).use { it.readBytes() }
        }

        return Archive(file.file).use { rar ->
            val header = rar.fileHeaders.find { it.fileName == entryName }
            rar.getInputStream(header).use { it.readBytes() }
        }
    }
}