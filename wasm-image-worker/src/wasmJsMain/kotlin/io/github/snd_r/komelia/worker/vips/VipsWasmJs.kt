package io.github.snd_r.komelia.worker.vips

import kotlinx.coroutines.asDeferred
import org.khronos.webgl.Uint8Array
import kotlin.js.Promise

internal const val vipsMaxSize = 10000000

internal external interface VipsImage : JsAny {
    val width: Int
    val height: Int
    val bands: Int
    val format: String
    val interpretation: String

    fun writeToMemory(): Uint8Array
    fun delete()
}

internal object Vips {
    private var vipsJs: JsAny? = null

    suspend fun init() {
        vipsJs = getVipsModule().asDeferred<JsAny>().await()
    }

    fun vipsThumbnail(
        buffer: Uint8Array,
        dstWidth: Int,
        dstHeight: Int,
        shouldCrop: Boolean
    ) = vipsThumbnail(requireNotNull(vipsJs), buffer, dstWidth, dstHeight, shouldCrop)

    fun vipsImageFromBuffer(buffer: Uint8Array) = vipsImageFromBuffer(requireNotNull(vipsJs), buffer)

    fun vipsImageDecode(buffer: Uint8Array) = vipsImageDecode(requireNotNull(vipsJs), buffer)
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
    let image = vips.Image.thumbnailBuffer (
            buffer,
    dstWidth,
    {
        height: dstHeight,
        size: 2, // only down
        ...(shouldCrop && { crop: 'entropy' })
    }
    );

    if (image.interpretation == 'b-w' && image.bands != 1 ||
        image.interpretation != 'srgb' && image.interpretation != 'b-w'
    ) {
        let old = image
                image = image.colourspace('srgb');
        old.delete()
    }
    if (image.interpretation == 'srgb' && image.bands == 3) {
        let old = image
                image = image.bandjoin(255)
        old.delete()
    }

    return image;
    """
    )
}

private fun vipsImageDecode(vips: JsAny, buffer: Uint8Array): VipsImage {
    js(
        """
        return vips.Image.newFromBuffer (buffer);
        """
    )
}

private fun vipsImageFromBuffer(vips: JsAny, buffer: Uint8Array): VipsImage {
    js(
        """
    let image = vips.Image.newFromBuffer(buffer);

    if (image.interpretation == 'b-w' && image.bands != 1 ||
        image.interpretation != 'srgb' && image.interpretation != 'b-w'
    ) {
        let old = image;
        image = image.colourspace('srgb');
        old.delete();
    }
    if (image.interpretation == 'srgb' && image.bands == 3) {
        let old = image;
        image = image.bandjoin(255);
        old.delete();
    }

    return image;
    """
    )
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
                        dynamicLibraries: [],
                        mainScriptUrlOrBlob: './vips.js',
                        locateFile: (fileName, scriptDirectory) => fileName,
                    });
                
                resolve(vips);
            } catch (err) {
                console.warn(err)
                reject(err);
            }
    }); 
    """
    )
}
