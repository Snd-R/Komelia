package snd.komelia.offline.mediacontainer.divina

import io.github.vinceglb.filekit.PlatformFile
import org.apache.commons.compress.archivers.zip.ZipFile

class ZipExtractor {
    fun getEntryBytes(file: PlatformFile, entryName: String): ByteArray {
        val zipFile = ZipFile
            .builder()
            .setFile(file.file)
            .setUseUnicodeExtraFields(true)
            .setIgnoreLocalFileHeader(true)
            .get()

        val bytes = zipFile.use { zip ->
            zip.getEntry(entryName)
                ?.let { entry -> zip.getInputStream(entry).use { it.readBytes() } }
        }

        if (bytes == null) throw IllegalStateException("zip entry does not exist: $entryName")
        return bytes
    }
}