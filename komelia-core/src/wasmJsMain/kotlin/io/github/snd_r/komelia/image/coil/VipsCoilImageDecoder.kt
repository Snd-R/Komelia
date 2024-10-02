package io.github.snd_r.komelia.image.coil

import coil3.ImageLoader
import coil3.asImage
import coil3.decode.DecodeResult
import coil3.decode.Decoder
import coil3.decode.ImageSource
import coil3.fetch.SourceFetchResult
import coil3.request.Options
import coil3.size.Dimension
import coil3.size.Scale
import coil3.size.isOriginal
import io.github.oshai.kotlinlogging.KotlinLogging
import io.github.snd_r.komelia.image.toBitmap
import io.github.snd_r.komelia.worker.ImageWorker
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive
import okio.use

private val logger = KotlinLogging.logger {}

class VipsCoilImageDecoder(
    private val source: ImageSource,
    private val options: Options,
    private val worker: ImageWorker
) : Decoder {

    override suspend fun decode(): DecodeResult {
        val dataResult = try {
            source.source().use { it.readByteArray() }
        } catch (e: Throwable) {
            currentCoroutineContext().ensureActive()
            throw RuntimeException(e)
        }

        val crop = options.scale == Scale.FILL
                && options.size.width != Dimension.Undefined
                && options.size.height != Dimension.Undefined

        val decoded = worker.decodeAndGet(
            bytes = dataResult,
            dstWidth = options.size.width.pxOrNull(),
            dstHeight = options.size.height.pxOrNull(),
            crop = crop
        )

        return DecodeResult(
            image = decoded.toBitmap().asImage(),
            isSampled = !options.size.isOriginal
        )
    }

    class Factory(
        private
        val worker: ImageWorker
    ) : Decoder.Factory {

        override fun create(
            result: SourceFetchResult,
            options: Options,
            imageLoader: ImageLoader,
        ): Decoder {
            return VipsCoilImageDecoder(result.source, options, worker)
        }
    }
}

private fun Dimension.pxOrNull(): Int? = if (this is Dimension.Pixels) px else null
