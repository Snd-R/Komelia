@file:OptIn(ExperimentalResourceApi::class)

package snd.komelia

import kotlinx.coroutines.await
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import org.jetbrains.compose.resources.ExperimentalResourceApi
import org.jetbrains.compose.resources.MissingResourceException
import org.jetbrains.compose.resources.ResourceReader
import org.khronos.webgl.ArrayBuffer
import org.w3c.fetch.Response
import org.w3c.files.Blob
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.js.Promise
import kotlin.wasm.unsafe.Pointer
import kotlin.wasm.unsafe.UnsafeWasmMemoryApi
import kotlin.wasm.unsafe.withScopedMemoryAllocator

// modified resource reader. Removes usage of cache to avoid errors when storing resource from extension url
// https://github.com/JetBrains/compose-multiplatform/blob/f7e7eebc31998c788f4ce5dea4f8c51c48ef8673/components/resources/library/src/webMain/kotlin/org/jetbrains/compose/resources/ResourceWebCache.web.kt#L65
// https://github.com/JetBrains/compose-multiplatform/blob/f7e7eebc31998c788f4ce5dea4f8c51c48ef8673/components/resources/library/src/wasmJsMain/kotlin/org/jetbrains/compose/resources/ResourceReader.wasmJs.kt#L59
internal object CustomWasmResourceReader : ResourceReader {
    private var cachedResponse: Blob? = null
    private val mutex = Mutex()
    override suspend fun read(path: String): ByteArray {
        return readAsBlob().asByteArray()
    }

    override suspend fun readPart(path: String, offset: Long, size: Long): ByteArray {
        val part = readAsBlob().slice(offset.toInt(), (offset + size).toInt())
        return part.asByteArray()
    }

    override fun getUri(path: String): String {
        return getResourceUrl()
    }

    private suspend fun readAsBlob(): Blob {
        cachedResponse?.let { return it }
        mutex.withLock {
            cachedResponse?.let { return it }
            val resPath = getResourceUrl()
            val response = try {
                cancellableFetch<Response>(resPath)
            } catch (_: Throwable) {
                throw MissingResourceException(resPath)
            }

            if (!response.ok) {
                throw MissingResourceException(resPath)
            }
            val blob = response.blob().await()
            cachedResponse = blob
            return blob
        }
    }

    private suspend fun Blob.asByteArray(): ByteArray {
        val buffer: ArrayBuffer = jsExportBlobAsArrayBuffer(this).await()
        return fastArrayBufferToByteArray(buffer)
    }
}

@OptIn(UnsafeWasmMemoryApi::class)
internal fun fastArrayBufferToByteArray(arrayBuffer: ArrayBuffer): ByteArray {
    val size = arrayBuffer.byteLength
    return withScopedMemoryAllocator { allocator ->
        val bufferPtr = allocator.allocate(size)
        copyArrayBufferToWasmMemory(arrayBuffer, bufferPtr.address.toInt())
        readFromLinearMemory(bufferPtr, 0, size)
    }
}

//language=js
@OptIn(ExperimentalWasmJsInterop::class)
private fun copyArrayBufferToWasmMemory(ab: ArrayBuffer, ptr: Int): Unit = js(
    """{
      const data = new Uint8Array(ab);
      new Uint8Array(wasmExports.memory.buffer).set(data, ptr);
}"""
)

@OptIn(UnsafeWasmMemoryApi::class)
private fun readFromLinearMemory(base: Pointer, offset: Int, length: Int): ByteArray {
    val bytes = ByteArray(length)
    val src = base + offset
    val intCount = length / 4
    var idx = 0
    for (i in 0 until intCount) {
        val value = (src + idx).loadInt()
        bytes[idx] = (value and 0xFF).toByte()
        bytes[idx + 1] = ((value shr 8) and 0xFF).toByte()
        bytes[idx + 2] = ((value shr 16) and 0xFF).toByte()
        bytes[idx + 3] = ((value shr 24) and 0xFF).toByte()
        idx += 4
    }
    for (i in idx until length) {
        bytes[i] = (src + i).loadByte()
    }
    return bytes
}

//language=js
private fun getResourceUrl(): String = js("{ return chrome.runtime.getURL('strings.commonMain.cvr'); }")

@JsFun("(blob) => blob.arrayBuffer()")
private external fun jsExportBlobAsArrayBuffer(blob: Blob): Promise<ArrayBuffer>

private external interface AbortSignal
private external class AbortController {
    val signal: AbortSignal
    fun abort()
}

@JsFun("(url, signal) => window.fetch(url, { signal })")
private external fun jsFetchWithSignal(url: String, signal: AbortSignal): Promise<Response>

@Suppress("UNCHECKED_CAST")
private suspend fun <T> cancellableFetch(url: String): T = suspendCancellableCoroutine { cont ->
    val ac = AbortController()
    jsFetchWithSignal(url, ac.signal).then(
        onFulfilled = { cont.resume(it as T); null },
        onRejected = {
            cont.resumeWithException(
                it.toThrowableOrNull() ?: error("Unexpected non-Kotlin exception $it")
            ); null
        }
    )
    cont.invokeOnCancellation { ac.abort() }
}
