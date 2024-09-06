package io.github.snd_r.komelia.worker.util

import org.khronos.webgl.ArrayBuffer
import org.khronos.webgl.Uint8Array
import org.khronos.webgl.get
import org.w3c.files.Blob

fun <T : JsAny> makeJsObject(): T = js("{ return {}; }")

@Suppress("UNUSED_PARAMETER")
private fun setObjectField(obj: JsAny, name: String, value: JsAny): Unit = js("obj[name]=value")

operator fun JsAny.set(name: String, value: JsAny) =
    setObjectField(this, name, value)

operator fun JsAny.set(name: String, value: String) =
    setObjectField(this, name, value.toJsString())

fun Uint8Array.asByteArray(): ByteArray =
    ByteArray(length) { this[it] }

@Suppress("UNUSED_PARAMETER")
private fun toJsArrayImpl(vararg x: Byte): Uint8Array = js("new Uint8Array(x)")

fun ByteArray.asJsArray(): Uint8Array = toJsArrayImpl(*this)

internal fun workerBufferTransferParam(bytes: ArrayBuffer): JsArray<JsAny> {
    js("return [bytes];")
}

fun toBlob(array: Uint8Array): Blob {
    js("return new Blob([array]);")
}