package snd.komelia.offline.mediacontainer.divina

import io.github.vinceglb.filekit.PlatformFile
import snd.komelia.offline.mediacontainer.DivinaExtractor

class DivinaZipExtractor(private val zipExtractor: ZipExtractor) : DivinaExtractor {
    override fun mediaTypes(): List<String> = listOf("application/zip")

    override fun getEntryBytes(file: PlatformFile, entryName: String): ByteArray {
        return zipExtractor.getEntryBytes(file, entryName)
    }
}