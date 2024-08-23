package io.github.snd_r.komelia.image.coil

import coil3.ImageLoader
import coil3.annotation.ExperimentalCoilApi
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
import kotlin.time.measureTimedValue

private val logger = KotlinLogging.logger {}

class VipsCoilImageDecoder(
    private val source: ImageSource,
    private val options: Options,
    private val worker: ImageWorker
) : Decoder {

    @OptIn(ExperimentalCoilApi::class)
    override suspend fun decode(): DecodeResult {
        val dataResult = measureTimedValue {
            try {
                source.source().use { it.readByteArray() }
            } catch (e: Throwable) {
                currentCoroutineContext().ensureActive()
                throw RuntimeException(e)
            }
        }.also { logger.info { "retrieved image bytes in ${it.duration}" } }

        val crop = options.scale == Scale.FILL
                && options.size.width != Dimension.Undefined
                && options.size.height != Dimension.Undefined

        val decoded = worker.decodeAndGet(
            bytes = dataResult.value,
            dstWidth = options.size.width.pxOrNull(),
            dstHeight = options.size.height.pxOrNull(),
            crop = crop
        )

        val decodeResult = measureTimedValue {
            DecodeResult(
                image = decoded.toBitmap().asImage(),
                isSampled = !options.size.isOriginal
            )
        }.also { logger.info { "installed pixels in ${it.duration}" } }

        return decodeResult.value
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
