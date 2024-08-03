package io.github.snd_r.komelia.image

import io.github.snd_r.komelia.image.ReaderImage.PageId
import io.github.snd_r.komelia.platform.UpscaleOption
import kotlinx.coroutines.flow.StateFlow
import kotlin.io.path.readBytes

class DesktopImageDecoder(
    private val upscaleOptionFlow: StateFlow<UpscaleOption>,
    private val onnxUpscaler: ManagedOnnxUpscaler?,
) : ImageDecoder {

    override fun decode(bytes: ByteArray, pageId: PageId): ReaderImage {
        return DesktopTilingReaderImage(
            encoded = bytes,
            pageId = pageId,
            upscaleOption = upscaleOptionFlow,
            upscaler = onnxUpscaler,
        )
    }

    override fun decode(cacheFile: okio.Path, pageId: PageId): ReaderImage {
        val bytes = cacheFile.toNioPath().readBytes()
        return DesktopTilingReaderImage(
            encoded = bytes,
            pageId = pageId,
            upscaleOption = upscaleOptionFlow,
            upscaler = onnxUpscaler,
        )
    }
}
