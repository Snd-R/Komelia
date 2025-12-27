package snd.komelia.offline.mediacontainer.divina

import io.github.vinceglb.filekit.PlatformFile
import snd.komelia.offline.mediacontainer.EpubExtractor

class EpubZipExtractor(private val zipExtractor: ZipExtractor) : EpubExtractor {
    override fun getEntryBytes(file: PlatformFile, entryName: String): ByteArray {
        return zipExtractor.getEntryBytes(file, entryName)
    }
}