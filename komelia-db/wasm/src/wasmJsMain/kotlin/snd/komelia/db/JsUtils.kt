package snd.komelia.db

import org.khronos.webgl.Uint8Array
import org.khronos.webgl.get

internal fun <T : JsAny> makeJsObject(): T = js("{ return {}; }")

@Suppress("UNUSED_PARAMETER")
private fun setObjectField(obj: JsAny, name: String, value: JsAny): Unit = js("obj[name]=value")

internal operator fun JsAny.set(name: String, value: JsAny) =
    setObjectField(this, name, value)

internal operator fun JsAny.set(name: String, value: String) =
    setObjectField(this, name, value.toJsString())

internal fun Uint8Array.asByteArray(): ByteArray =
    ByteArray(length) { this[it] }
