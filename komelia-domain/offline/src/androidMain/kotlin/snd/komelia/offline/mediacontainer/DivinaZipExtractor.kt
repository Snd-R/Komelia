package snd.komelia.offline.mediacontainer

import io.github.vinceglb.filekit.PlatformFile

class DivinaZipExtractor(private val zipExtractor: ZipExtractor) : DivinaExtractor {
    override fun mediaTypes(): List<String> = listOf("application/zip")

    override fun getEntryBytes(file: PlatformFile, entryName: String): ByteArray {
        return zipExtractor.getEntryBytes(file, entryName)
    }
}