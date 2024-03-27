package io.github.snd_r.komelia.image

import org.apache.tika.config.TikaConfig
import org.apache.tika.io.TikaInputStream
import org.apache.tika.metadata.Metadata
import java.io.InputStream
import java.nio.file.Path
import kotlin.io.path.name


private const val avif = "image/avif"
private const val gif = "image/gif"
private const val jpeg = "image/jpeg"
private const val png = "image/png"
private const val webp = "image/webp"
val supportedImageMimeTypes = setOf(avif, gif, jpeg, png, webp)

object ImageTypeDetector {
    private val tika = TikaConfig()
    fun isSupportedImageType(path: Path): Boolean {
        val metadata = Metadata()
        metadata[Metadata.TIKA_MIME_FILE] = path.name

        return TikaInputStream.get(path).use { stream -> isSupportedImageType(stream, metadata) }
    }

    fun isSupportedImageType(bytes: ByteArray): Boolean {
        return isSupportedImageType(bytes.inputStream())
    }

    private fun isSupportedImageType(stream: InputStream, metadata: Metadata = Metadata()): Boolean {
        val mimeType = tika.detector.detect(stream, metadata)
        return mimeType.toString() in supportedImageMimeTypes
    }
}