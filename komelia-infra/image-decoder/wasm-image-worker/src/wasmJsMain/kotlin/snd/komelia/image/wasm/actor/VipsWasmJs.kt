package snd.komelia.image.wasm.actor

import kotlinx.coroutines.asDeferred
import org.khronos.webgl.Uint8Array
import org.w3c.files.Blob
import snd.komelia.image.wasm.makeJsObject
import snd.komelia.image.wasm.set
import kotlin.js.Promise

internal const val vipsMaxSize = 10_000_000

internal external class VipsImage : JsAny {
    val width: Int
    val height: Int
    val bands: Int
    val format: String
    val interpretation: Interpretation

    fun colourspace(space: Interpretation): VipsImage
    fun bandjoin(bandValue: Int, options: JsAny?): VipsImage
    fun writeToMemory(): Uint8Array
    fun thumbnailImage(width: Int, options: ThumbnailOptions?): VipsImage
    fun extractArea(left: Int, top: Int, width: Int, height: Int): VipsImage
    fun shrink(hshrink: Double, vshrink: Double, options: JsAny?): VipsImage
    fun findTrim(options: FindTrimOptions): VipsRect
    fun histFind(options: HistFindOptions?): VipsImage
    fun histNorm(): VipsImage
    fun mapLut(lut: VipsImage, options: JsAny?): VipsImage
    fun clone(): VipsImage
    fun delete()
}

internal external interface HistFindOptions : JsAny {
    val bands: Int?
}

internal fun histFindOptions(bands: Int): HistFindOptions {
    val options = makeJsObject<HistFindOptions>()
    options["bands"] = bands.toJsNumber()
    return options
}

internal external interface FindTrimOptions : JsAny {
    val threshold: Double?

    @JsName("line_art")
    val lineArt: Boolean
}

internal fun findTrimOptions(
    threshold: Double?,
    lineArt: Boolean?
): FindTrimOptions {
    val options = makeJsObject<FindTrimOptions>()

    threshold?.let { options["threshold"] = it.toJsNumber() }
    lineArt?.let { options["line_art"] = it.toJsBoolean() }

    return options

}

internal external interface VipsRect {
    val left: Int
    val top: Int
    val width: Int
    val height: Int
}

internal external interface ThumbnailOptions : JsAny {
    val height: Int?
    val size: String?
    val crop: String?
}

internal fun thumbnailOptions(height: Int?, crop: Boolean): ThumbnailOptions {
    val options = makeJsObject<ThumbnailOptions>()

    height?.let { options["height"] = it.toJsNumber() }
    if (crop) options["crop"] = "entropy"

    return options
}

@JsName("null")
@Suppress("NESTED_CLASS_IN_EXTERNAL_INTERFACE")
internal external interface Interpretation : JsAny {
    companion object
}

internal inline val Interpretation.Companion.B_W: Interpretation get() = "b-w".toJsString().unsafeCast()
internal inline val Interpretation.Companion.sRGB: Interpretation get() = "srgb".toJsString().unsafeCast()
internal inline val Interpretation.Companion.HISTOGRAM: Interpretation get() = "histogram".toJsString().unsafeCast()

@JsName("null")
@Suppress("NESTED_CLASS_IN_EXTERNAL_INTERFACE")
internal external interface BandFormat : JsAny {
    companion object
}

internal inline val BandFormat.Companion.UCHAR: BandFormat get() = "uchar".toJsString().unsafeCast()
internal inline val BandFormat.Companion.CHAR: BandFormat get() = "char".toJsString().unsafeCast()
internal inline val BandFormat.Companion.UINT: BandFormat get() = "uint".toJsString().unsafeCast()


internal object Vips {
    private var vipsJs: JsAny? = null

    suspend fun init() {
        vipsJs = getVipsModule().asDeferred<JsAny>().await()
    }

    fun thumbnail(
        buffer: Uint8Array,
        dstWidth: Int,
        dstHeight: Int,
        shouldCrop: Boolean
    ) = vipsThumbnail(requireNotNull(vipsJs), buffer, dstWidth, dstHeight, shouldCrop)

    fun newImageFromBuffer(buffer: Uint8Array) = vipsImageFromBuffer(requireNotNull(vipsJs), buffer)
    fun newImageFromMemory(
        memory: Blob,
        width: Int,
        height: Int,
        bands: Int,
        format: BandFormat
    ) = vipsImageFromMemory(requireNotNull(vipsJs), memory, width, height, bands, format)
}

private fun vipsThumbnail(
    vips: JsAny,
    buffer: Uint8Array,
    dstWidth: Int,
    dstHeight: Int,
    shouldCrop: Boolean
): VipsImage {
    js(
        """
    let image = vips.Image.thumbnailBuffer(
        buffer,
        dstWidth,
        {
            height: dstHeight,
            size: 2, // only down
            ...(shouldCrop && { crop: 'entropy' })
        }
    );
    return image;
    """
    )
}

private fun vipsImageDecode(vips: JsAny, buffer: Uint8Array): VipsImage {
    js("return vips.Image.newFromBuffer(buffer);")
}

private fun vipsImageFromBuffer(vips: JsAny, buffer: Uint8Array): VipsImage {
    js("return vips.Image.newFromBuffer(buffer);")
}

private fun vipsImageFromMemory(
    vips: JsAny,
    memory: Blob,
    width: Int,
    height: Int,
    bands: Int,
    format: BandFormat
): VipsImage {
    js("return vips.Image.newFromMemory(memory, width, height, bands, format);")
}

private fun getVipsModule(): Promise<JsAny> {
    js(
        """
        return new Promise(async (resolve, reject) => {
            const VipsCreateModule = typeof globalThis.Vips === 'undefined' ? null : globalThis.Vips;
            if (!VipsCreateModule) {
                reject(new Error('Module Not Loaded'));
                return;
            }
            try {
                    let vips = await VipsCreateModule({
                        dynamicLibraries: ['vips-jxl.wasm'],
                        mainScriptUrlOrBlob: './vips.js',
                        locateFile: (fileName, scriptDirectory) => fileName,
                    });
                    vips.Cache.max(0);

                resolve(vips);
            } catch (err) {
                console.warn(err)
                reject(err);
            }
    });
    """
    )
}
