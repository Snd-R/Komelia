package snd.komelia.offline.mediacontainer

import io.github.vinceglb.filekit.PlatformFile

interface DivinaExtractor {
    fun mediaTypes(): List<String>

    fun getEntryBytes(file: PlatformFile, entryName: String): ByteArray
}