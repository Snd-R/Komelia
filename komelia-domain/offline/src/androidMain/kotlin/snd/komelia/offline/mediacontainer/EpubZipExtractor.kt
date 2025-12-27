package snd.komelia.offline.mediacontainer

import io.github.vinceglb.filekit.PlatformFile

class EpubZipExtractor(private val zipExtractor: ZipExtractor) : EpubExtractor {
    override fun getEntryBytes(file: PlatformFile, entryName: String): ByteArray {
        return zipExtractor.getEntryBytes(file, entryName)
    }
}