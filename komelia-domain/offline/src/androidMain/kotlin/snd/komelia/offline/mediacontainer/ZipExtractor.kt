package snd.komelia.offline.mediacontainer

import io.github.vinceglb.filekit.AndroidFile
import io.github.vinceglb.filekit.FileKit
import io.github.vinceglb.filekit.PlatformFile
import io.github.vinceglb.filekit.context
import org.apache.commons.compress.archivers.zip.ZipFile

class ZipExtractor {
    fun getEntryBytes(file: PlatformFile, entryName: String): ByteArray {
        val zipFileBuilder = ZipFile
            .builder()
            .setUseUnicodeExtraFields(true)
            .setIgnoreLocalFileHeader(true)

        when (val androidFile = file.androidFile) {
            is AndroidFile.FileWrapper -> zipFileBuilder.file = androidFile.file
            is AndroidFile.UriWrapper -> zipFileBuilder.setSeekableByteChannel(
                SafSeekableReadByteChannel(androidFile.uri, FileKit.context)
            )
        }

        val zipFile = zipFileBuilder.get()
        val bytes = zipFile.use { zip ->
            zip.getEntry(entryName)
                ?.let { entry -> zip.getInputStream(entry).use { it.readBytes() } }
        }

        if (bytes == null) error("zip entry does not exist: $entryName")
        return bytes
    }
}