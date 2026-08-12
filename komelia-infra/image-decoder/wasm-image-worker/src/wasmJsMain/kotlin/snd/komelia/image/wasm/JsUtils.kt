package snd.komelia.image.wasm

import org.khronos.webgl.ArrayBuffer
import org.khronos.webgl.Uint8Array
import org.w3c.files.Blob
import kotlin.wasm.unsafe.Pointer
import kotlin.wasm.unsafe.UnsafeWasmMemoryApi
import kotlin.wasm.unsafe.withScopedMemoryAllocator

fun <T : JsAny> makeJsObject(): T = js("{ return {}; }")

@Suppress("UNUSED_PARAMETER")
private fun setObjectField(obj: JsAny, name: String, value: JsAny): Unit = js("obj[name]=value")

operator fun JsAny.set(name: String, value: JsAny) =
    setObjectField(this, name, value)

operator fun JsAny.set(name: String, value: String) =
    setObjectField(this, name, value.toJsString())

@Suppress("UNUSED_PARAMETER")
private fun toJsArrayImpl(vararg x: Byte): Uint8Array = js("new Uint8Array(x)")

fun ByteArray.toJsArray(): Uint8Array = toJsArrayImpl(*this)

internal fun jsArray(element: JsAny): JsArray<JsAny> {
    js("return [element];")
}

internal fun jsArray(element1: JsAny, element2: JsAny): JsArray<JsAny> {
    js("return [element1, element2];")
}

fun toBlob(array: Uint8Array): Blob {
    js("return new Blob([array]);")
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
