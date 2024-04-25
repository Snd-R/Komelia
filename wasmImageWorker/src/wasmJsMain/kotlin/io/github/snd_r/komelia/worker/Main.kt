package io.github.snd_r.komelia.worker

import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.asDeferred
import kotlinx.coroutines.launch
import org.khronos.webgl.Int8Array
import org.khronos.webgl.Uint8Array
import org.w3c.dom.DedicatedWorkerGlobalScope
import kotlin.js.Promise
import kotlin.time.measureTime


const val InitMessage = "Init"
const val DecodeMessage = "Decode"

external val self: DedicatedWorkerGlobalScope

private const val vipsMaxSize = 10000000
private var vips: JsAny? = null

@OptIn(DelicateCoroutinesApi::class)
fun main() {
    self.importScripts("vips.js")
    GlobalScope.launch {
        vips = getVipsModule().asDeferred<JsAny>().await()

        self.onmessage = { message ->
            val type = getMessageType(message.data)
            when (type) {
                InitMessage -> self.postMessage(initMessage())
                DecodeMessage -> if (message.data != null) handleDecode(message.data!!)
                else -> {}
            }
        }
    }
}

private fun handleDecode(data: JsAny) {
    val duration = measureTime {
        val vips = requireNotNull(vips)
        val requestId = getDecodeId(data)
        val requestWidth = getDecodeWidth(data)
        val requestHeight = getDecodeHeight(data)
        val requestCrop = getDecodeCrop(data)
        val requestBuffer = getDecodeBuffer(data)

        val decodedImage = if (requestWidth == null && requestHeight == null) {
            vipsImageFromBuffer(vips, requestBuffer)
        } else {
            val dstWidth = requestWidth ?: vipsMaxSize
            val dstHeight = requestHeight ?: vipsMaxSize
            vipsThumbnail(vips, requestBuffer, dstWidth, dstHeight, requestCrop)
        }
        val decodedBytes = vipsGetBytes(decodedImage)

        self.postMessage(
            decodeResponse(
                responseId = requestId,
                responseWidth = vipsGetWidth(decodedImage),
                responseHeight = vipsGetHeight(decodedImage),
                responseBands = vipsGetBands(decodedImage),
                responseInterpretation = vipsGetInterpretation(decodedImage),
                bytes = decodedBytes,
            ),
            decodeResponseTransfer(decodedBytes)
        )
        vipsImageDelete(decodedImage)
    }
    println("Worker finished image decode in $duration")
}

private fun getMessageType(data: JsAny?): String? {
    js("return data.type;")
}

private fun getDecodeId(data: JsAny): Int {
    js("return data.id;")
}

private fun getDecodeWidth(data: JsAny): Int? {
    js("return data.width;")
}

private fun getDecodeHeight(data: JsAny): Int? {
    js("return data.height;")
}

private fun getDecodeCrop(data: JsAny): Boolean {
    js("return data.crop;")
}

private fun getDecodeBuffer(data: JsAny): Int8Array {
    js("return data.buffer;")
}

private fun decodeResponse(
    responseId: Int,
    responseWidth: Int,
    responseHeight: Int,
    responseBands: Int,
    responseInterpretation: String,
    bytes: Uint8Array,
): JsAny {
    js(
        """
        return { type: 'Decode',
                 id: responseId,
                 width: responseWidth,
                 height: responseHeight,
                 bands: responseBands,
                 interpretation: responseInterpretation,
                 buffer: bytes,
        };
    """
    )
}

private fun decodeResponseTransfer(bytes: Uint8Array): JsArray<JsAny> {
    js("return [bytes.buffer];")
}

private fun vipsThumbnail(vips: JsAny, buffer: Int8Array, dstWidth: Int, dstHeight: Int, shouldCrop: Boolean): JsAny {
    js(
        """
    let image = vips.Image.thumbnailBuffer (
            buffer,
    dstWidth,
    {
        height: dstHeight,
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

private fun vipsImageFromBuffer(vips: JsAny, buffer: Int8Array): JsAny {

    js(
        """
    let image = vips.Image.newFromBuffer (buffer);

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

private fun vipsGetInterpretation(image: JsAny): String {
    js(
        """
    return image.interpretation;
    """
    )
}

private fun vipsGetBytes(image: JsAny): Uint8Array {
    js(
        """
    return image.writeToMemory();
    """
    )
}

private fun vipsGetBands(image: JsAny): Int {
    js(
        """
    return image.bands;
    """
    )
}

private fun vipsGetWidth(image: JsAny): Int {
    js(
        """
    return image.width;
    """
    )
}

private fun vipsGetHeight(image: JsAny): Int {
    js(
        """
    return image.height;
    """
    )
}

private fun vipsImageDelete(image: JsAny): Int {
    js(
        """
    return image.delete();
    """
    )
}

private fun initMessage(): JsAny {
    js("return {type: 'Init'}")
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
